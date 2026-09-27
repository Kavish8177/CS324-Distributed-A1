package fj.usp.cs324.distrilab.bootstrap;

import fj.usp.cs324.distrilab.common.WorkerInfo;
import fj.usp.cs324.distrilab.remote.BootstrapService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BootstrapServer extends UnicastRemoteObject implements BootstrapService {
    public static final String BINDING = "DistriLabBootstrap";
    private final Map<Integer, WorkerInfo> activeWorkers = new ConcurrentHashMap<>();

    public BootstrapServer() throws RemoteException {
        super(0);
    }

    @Override
    public synchronized WorkerInfo registerWorker(WorkerInfo worker) throws RemoteException {
        if (activeWorkers.containsKey(worker.id())) {
            throw new RemoteException("Worker ID " + worker.id() + " is already registered");
        }
        List<WorkerInfo> existing = new ArrayList<>(activeWorkers.values());
        WorkerInfo attachment = existing.isEmpty() ? null
                : existing.get(ThreadLocalRandom.current().nextInt(existing.size()));
        activeWorkers.put(worker.id(), worker);
        System.out.printf("Registered worker %d at %s:%d; attach to %s%n",
                worker.id(), worker.host(), worker.registryPort(),
                attachment == null ? "nobody (first worker)" : "worker " + attachment.id());
        return attachment;
    }

    @Override
    public void updateWorker(WorkerInfo worker) {
        activeWorkers.computeIfPresent(worker.id(), (id, old) -> worker);
    }

    @Override
    public void unregisterWorker(int workerId) {
        activeWorkers.remove(workerId);
        System.out.println("Unregistered worker " + workerId);
    }

    @Override
    public List<WorkerInfo> getActiveWorkers() {
        return List.copyOf(activeWorkers.values());
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 1099;
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind(BINDING, new BootstrapServer());
            System.out.println("Bootstrap Node ready on port " + port);
        } catch (Exception e) {
            System.err.println("Could not start Bootstrap Node: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
