# CS324 Distributed Systems – DistriLab

## Project Overview

DistriLab is a distributed computing system developed for CS324 Distributed Systems Assignment 1.

The system uses Java RMI to allow multiple worker nodes to communicate and perform computational jobs. Workers form an unstructured network and elect a coordinator to manage incoming jobs.

The system consists of:

- Bootstrap Server
- Worker Nodes
- Leader Election
- Client GUI
- Distributed Job Processing

## Requirements

- Java JDK 21 or later
- Apache NetBeans
- Git
- Java RMI

## Project Structure

The main packages are:

- `bootstrap` – worker registration and discovery
- `client` – graphical client interface
- `common` – shared messages and job objects
- `remote` – Java RMI interfaces
- `worker` – worker nodes, job processing and leader election

## How to Run

The Bootstrap Server must be started first, followed by the worker nodes and then the client.

### 1. Start Bootstrap Server

Run:

`fj.usp.cs324.distrilab.bootstrap.BootstrapServer`

The Bootstrap Server uses port `1099`.

Expected output:

```text
Bootstrap Node ready on port 1099
```

### 2. Start Worker 1

Run WorkerServer with:

```text
1 1101 localhost 1099 localhost
```

Expected output will include:

```text
Worker 1 ready on localhost:1101 with leaderman=cs324
```

If no coordinator exists, the worker can begin a leader election.

### 3. Start Worker 2

Open another terminal from the project directory and run:

```text
java -cp build\classes fj.usp.cs324.distrilab.worker.WorkerServer 2 1102 localhost 1099 localhost
```

Worker 2 should register with the Bootstrap Server and connect to an active worker.

Example:

```text
Worker 2 connected to worker 1
Worker 2 ready on localhost:1102 with leaderman=cs324
```

Additional workers can be started using different worker IDs and registry ports.

### 4. Start the Client

Run:

```text
java -cp build\classes fj.usp.cs324.distrilab.client.ClientGui
```

The DistriLab Client GUI will open.

## Supported Jobs

### MAX

Finds the maximum value from a collection of integers.

Example input:

```text
10, 25, 7, 99, 42, 3, 61
```

Expected result:

```text
99
```

### PRIMECOUNT

Counts the prime numbers in a collection of integers.

Example input:

```text
2, 3, 4, 5, 6, 7, 8, 9, 10, 11
```

Expected result:

```text
5
```

### PRIMESUM

Calculates the sum of prime numbers within a specified range.

Example range:

```text
1 to 10
```

Expected result:

```text
17
```

## Distributed Processing

The coordinator receives jobs from clients and divides the workload between the available workers.

For example, with two active workers:

```text
Coordinator 1 assigned chunk 1 to worker 1
Coordinator 1 assigned chunk 2 to worker 2
```

Workers can process assigned jobs concurrently.

## Leader Election

When there is no active coordinator, workers can initiate an election.

Candidates are compared using their JAC value. The worker with the lowest JAC is preferred. If workers have the same JAC, the worker with the highest worker ID is selected.

After a coordinator completes five jobs, a new leader election is started.

Example:

```text
Coordinator 1 completed five jobs; beginning a new election
Worker 1 started election
Worker 2 accepted COORDINATOR 2
```

The newly elected coordinator can then receive and distribute new client jobs.

## Client Input

The client supports:

- Manual input
- CSV input
- Multiple job types
- Concurrent job submissions
- Display of completed job results

## Team Contributions

The project was developed collaboratively using Git branches and pull requests.

- Kavish – Bootstrap and network components
- Aditya – Leader election components
- Atharv – Job processing and client components

Integration and testing were performed after merging the individual branches into `main`.

## Testing

The integrated system was tested using two worker processes.

The following operations were verified:

- Worker registration and discovery
- Worker-to-worker connection
- Leader election
- MAX job
- PRIMECOUNT job
- PRIMESUM job
- Workload distribution between multiple workers
- New leader election after five coordinator jobs
- Successful job processing after coordinator change
