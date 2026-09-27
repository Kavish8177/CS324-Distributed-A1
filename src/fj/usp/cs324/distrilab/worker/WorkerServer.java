package fj.usp.cs324.distrilab.worker;

import fj.usp.cs324.distrilab.bootstrap.BootstrapServer;
import fj.usp.cs324.distrilab.common.*;
import fj.usp.cs324.distrilab.remote.BootstrapService;
import fj.usp.cs324.distrilab.remote.WorkerService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerServer extends UnicastRemoteObject implements WorkerService {
    private final int id;
    private final String host;
    private final int registryPort;
    private final BootstrapService bootstrap;
    private final Map<Integer, WorkerInfo> neighbours = new ConcurrentHashMap<>();
    private final Set<UUID> processedElections = ConcurrentHashMap.newKeySet();
    private final Set<UUID> processedCoordinatorMessages = ConcurrentHashMap.newKeySet();
    private final AtomicInteger jac = new AtomicInteger();
    private final AtomicBoolean electionStarting = new AtomicBoolean(false);
    private final ExecutorService computationPool = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors()));
    private final ExecutorService dispatchPool = Executors.newCachedThreadPool();
    private final Object termLock = new Object();
    private volatile int coordinatorId = -1;
    private int termJobCount = 0;

    // Explicit assignment requirement: this variable must exist in worker nodes.
    private final String leaderman = "cs324";

    public WorkerServer(int id, String host, int registryPort, BootstrapService bootstrap)
            throws RemoteException {
        super(0);
        this.id = id;
        this.host = host;
        this.registryPort = registryPort;
        this.bootstrap = bootstrap;
    }

    @Override
    public WorkerInfo getInfo() {
        return currentInfo();
    }

    private WorkerInfo currentInfo() {
        return new WorkerInfo(id, host, registryPort, jac.get());
    }

    @Override
    public void addNeighbour(WorkerInfo worker) {
        if (worker.id() != id) {
            neighbours.put(worker.id(), worker);
            System.out.printf("Worker %d connected to worker %d%n", id, worker.id());
        }
    }

    @Override
    public void removeNeighbour(int workerId) {
        neighbours.remove(workerId);
    }

    @Override
    public Candidate receiveElection(ElectionMessage message) {
        if (!processedElections.add(message.electionId())) {
            return null;
        }

        System.out.printf("Worker %d processing ELECTION %s from worker %d%n",
                id, shortId(message.electionId()), message.senderId());
        Candidate best = new Candidate(id, jac.get());

        for (WorkerInfo neighbour : List.copyOf(neighbours.values())) {
            if (neighbour.id() == message.senderId()) continue;
            try {
                Candidate candidate = RmiLookup.worker(neighbour).receiveElection(
                        new ElectionMessage(message.electionId(), message.initiatorId(), id));
                best = Candidate.better(best, candidate);
            } catch (Exception e) {
                neighbours.remove(neighbour.id());
                System.err.printf("Worker %d skipped inactive neighbour %d during election%n",
                        id, neighbour.id());
            }
        }
        return best;
    }

    @Override
    public void startElection() throws RemoteException {
        if (!electionStarting.compareAndSet(false, true)) return;
        try {
            UUID electionId = UUID.randomUUID();
            System.out.printf("Worker %d started election %s%n", id, shortId(electionId));
            Candidate winner = receiveElection(new ElectionMessage(electionId, id, -1));
            if (winner == null) throw new RemoteException("Election produced no winner");
            receiveCoordinator(new CoordinatorMessage(electionId, winner.workerId(), -1));
        } finally {
            electionStarting.set(false);
        }
    }

    @Override
    public void receiveCoordinator(CoordinatorMessage message) {
        if (!processedCoordinatorMessages.add(message.electionId())) return;

        boolean beginningNewLeadership = coordinatorId != id && message.coordinatorId() == id;
        coordinatorId = message.coordinatorId();
        if (beginningNewLeadership) {
            synchronized (termLock) {
                termJobCount = 0;
            }
        }
        System.out.printf("Worker %d accepted COORDINATOR %d for election %s%n",
                id, coordinatorId, shortId(message.electionId()));

        for (WorkerInfo neighbour : List.copyOf(neighbours.values())) {
            if (neighbour.id() == message.senderId()) continue;
            dispatchPool.execute(() -> {
                try {
                    RmiLookup.worker(neighbour).receiveCoordinator(
                            new CoordinatorMessage(message.electionId(), message.coordinatorId(), id));
                } catch (Exception e) {
                    neighbours.remove(neighbour.id());
                }
            });
        }
    }

    @Override
    public int getCoordinatorId() {
        return coordinatorId;
    }

    @Override
    public JobResult submitJob(JobRequest request) throws RemoteException {
        synchronized (termLock) {
            if (coordinatorId != id) {
                throw new RemoteException("Worker " + id + " is not the coordinator; coordinator is "
                        + coordinatorId);
            }
            if (termJobCount >= 5) {
                throw new RemoteException("This coordinator's five-job term has ended. Retry shortly.");
            }
            termJobCount++;
            System.out.printf("Coordinator %d accepted job %s (%d/5 this term)%n",
                    id, shortId(request.jobId()), termJobCount);
        }

        List<WorkerInfo> workers;
        try {
            workers = new ArrayList<>(bootstrap.getActiveWorkers());
        } catch (Exception e) {
            throw new RemoteException("Could not obtain active workers", e);
        }
        workers.sort(Comparator.comparingInt(WorkerInfo::id));
        if (workers.isEmpty()) throw new RemoteException("No active workers are available");

        List<JobChunk> chunks;
        try {
            chunks = JobSplitter.split(request, workers.size());
        } catch (IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        }

        List<Future<JobResult>> futures = new ArrayList<>();
        for (int index = 0; index < chunks.size(); index++) {
            JobChunk chunk = chunks.get(index);
            WorkerInfo target = workers.get(index % workers.size());
            if (target.id() != id) {
                jac.incrementAndGet();
                try {
                    bootstrap.updateWorker(currentInfo());
                } catch (Exception e) {
                    System.err.println("Warning: could not publish updated JAC: " + e.getMessage());
                }
            }
            futures.add(dispatchPool.submit(() -> {
                System.out.printf("Coordinator %d assigned chunk %d to worker %d%n",
                        id, chunk.chunkNumber(), target.id());
                return target.id() == id ? executeChunk(chunk)
                        : RmiLookup.worker(target).executeChunk(chunk);
            }));
        }

        List<JobResult> partialResults = new ArrayList<>();
        try {
            for (Future<JobResult> future : futures) partialResults.add(future.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Job interrupted", e);
        } catch (ExecutionException e) {
            throw new RemoteException("A worker failed while processing the job", e.getCause());
        }

        long finalValue = JobMath.combine(request.type(), partialResults);
        JobResult finalResult = new JobResult(request.jobId(), request.type(), finalValue,
                "Combined " + partialResults.size() + " worker result(s)");

        boolean termEnded;
        synchronized (termLock) {
            termEnded = termJobCount >= 5;
            if (termEnded) coordinatorId = -1;
        }
        if (termEnded) {
            System.out.printf("Coordinator %d completed five jobs; beginning a new election%n", id);
            dispatchPool.execute(() -> {
                try {
                    startElection();
                } catch (RemoteException e) {
                    System.err.println("Automatic election failed: " + e.getMessage());
                }
            });
        }
        return finalResult;
    }

    @Override
    public JobResult executeChunk(JobChunk chunk) throws RemoteException {
        try {
            return computationPool.submit(() -> JobMath.calculate(chunk)).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Chunk interrupted", e);
        } catch (ExecutionException e) {
            throw new RemoteException("Chunk calculation failed", e.getCause());
        }
    }

    private static String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: WorkerServer <workerId> <registryPort> [bootstrapHost] [bootstrapPort] [advertisedHost]");
            return;
        }
        int id = Integer.parseInt(args[0]);
        int workerPort = Integer.parseInt(args[1]);
        String bootstrapHost = args.length > 2 ? args[2] : "localhost";
        int bootstrapPort = args.length > 3 ? Integer.parseInt(args[3]) : 1099;
        String advertisedHost = args.length > 4 ? args[4] : "localhost";

        try {
            System.setProperty("java.rmi.server.hostname", advertisedHost);
            BootstrapService bootstrap = (BootstrapService) LocateRegistry
                    .getRegistry(bootstrapHost, bootstrapPort).lookup(BootstrapServer.BINDING);
            Registry registry = LocateRegistry.createRegistry(workerPort);
            WorkerServer server = new WorkerServer(id, advertisedHost, workerPort, bootstrap);
            registry.rebind(server.currentInfo().bindingName(), server);

            WorkerInfo attachment = bootstrap.registerWorker(server.currentInfo());
            if (attachment != null) {
                server.addNeighbour(attachment);
                RmiLookup.worker(attachment).addNeighbour(server.currentInfo());
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    bootstrap.unregisterWorker(id);
                    for (WorkerInfo neighbour : server.neighbours.values()) {
                        try { RmiLookup.worker(neighbour).removeNeighbour(id); }
                        catch (Exception ignored) { }
                    }
                } catch (Exception ignored) { }
                server.computationPool.shutdownNow();
                server.dispatchPool.shutdownNow();
            }));

            System.out.printf("Worker %d ready on %s:%d with leaderman=%s%n",
                    id, advertisedHost, workerPort, server.leaderman);
            if (attachment == null) {
                server.startElection();
            } else {
                int existingCoordinator = RmiLookup.worker(attachment).getCoordinatorId();
                if (existingCoordinator < 0) {
                    server.startElection();
                } else {
                    server.receiveCoordinator(new CoordinatorMessage(
                            UUID.randomUUID(), existingCoordinator, attachment.id()));
                }
            }
        } catch (Exception e) {
            System.err.println("Could not start worker: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
