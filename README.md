# CS324 Distributed Systems – DistriLab

## Project Overview

DistriLab is a distributed computing system developed for CS324 Distributed Systems Assignment 1.

The system uses Java RMI to allow multiple worker nodes to communicate and perform computational jobs. Workers form an unstructured network and elect a coordinator to manage incoming jobs and distribute computational work between active workers.

The system consists of:

- Bootstrap Server
- Worker Nodes
- Leader Election
- Client GUI
- Distributed Job Processing
- Concurrent Job Execution

---

## Requirements

Before running the project, ensure the following are installed:

- Java JDK 21 or later
- Apache NetBeans
- Apache Ant
- Git

The project uses Java RMI for communication between distributed components.

---

# Building the Project

Open Command Prompt and navigate to the project directory.

Example:

```bat
cd /d "C:\path\to\CS324-Distributed-A1"
```

Build the complete project using:

```bat
ant clean jar
```

Alternatively, the project can be rebuilt through NetBeans using:

**Run → Clean and Build Project**

A successful build should end with:

```text
BUILD SUCCESSFUL
```

The compiled classes will be located in:

```text
build\classes
```

---

# Important Startup Order

Start the distributed system in the following order:

1. Bootstrap Server
2. Worker 1
3. Worker 2
4. Additional workers if required
5. Client GUI

Keep the Bootstrap Server and all worker terminals running while testing the system.

---

# 1. Start the Bootstrap Server

The Bootstrap Server must be started before any workers.

## Option A – NetBeans

Run:

```text
fj.usp.cs324.distrilab.bootstrap.BootstrapServer
```

## Option B – Command Prompt

From the project directory run:

```bat
java -cp build\classes fj.usp.cs324.distrilab.bootstrap.BootstrapServer
```

Expected output:

```text
Bootstrap Node ready on port 1099
```

The Bootstrap Server maintains information about active worker nodes.

Leave the Bootstrap Server running.

---

# 2. Start Worker 1

Open a new Command Prompt.

Navigate to the project directory:

```bat
cd /d "C:\path\to\CS324-Distributed-A1"
```

Run:

```bat
java -cp build\classes fj.usp.cs324.distrilab.worker.WorkerServer 1 1101 localhost 1099 localhost
```

The arguments represent:

```text
Worker ID = 1
Worker RMI Port = 1101
Bootstrap Host = localhost
Bootstrap Port = 1099
Worker Host = localhost
```

Expected output will include:

```text
Worker 1 ready on localhost:1101 with leaderman=cs324
```

If no coordinator exists, Worker 1 can initiate an election.

Example:

```text
Worker 1 started election
Worker 1 processing ELECTION
Worker 1 accepted COORDINATOR 1
```

Leave Worker 1 running.

---

# 3. Start Worker 2

Open another Command Prompt.

Navigate to the project directory:

```bat
cd /d "C:\path\to\CS324-Distributed-A1"
```

Run:

```bat
java -cp build\classes fj.usp.cs324.distrilab.worker.WorkerServer 2 1102 localhost 1099 localhost
```

Expected output will include:

```text
Worker 2 connected to worker 1
Worker 2 ready on localhost:1102 with leaderman=cs324
Worker 2 accepted COORDINATOR 1
```

Worker 2 registers with the Bootstrap Server and connects to an active worker.

Leave Worker 2 running.

---

# 4. Optional – Start Additional Workers

Additional workers can be started using unique worker IDs and unique RMI ports.

For example, Worker 3:

```bat
java -cp build\classes fj.usp.cs324.distrilab.worker.WorkerServer 3 1103 localhost 1099 localhost
```

Worker 4:

```bat
java -cp build\classes fj.usp.cs324.distrilab.worker.WorkerServer 4 1104 localhost 1099 localhost
```

Do not reuse a worker ID or RMI port that is already running.

---

# 5. Start the Client GUI

Open another Command Prompt.

Navigate to the project directory:

```bat
cd /d "C:\path\to\CS324-Distributed-A1"
```

Run:

```bat
java -cp build\classes fj.usp.cs324.distrilab.client.ClientGui
```

The DistriLab Distributed Computing Client should open.

The GUI provides:

- Job Type selection
- Range Start
- Range End
- Manual Input Data
- CSV loading
- Submit Job button
- Concurrent Job Output
- Completed job results

---

# TEST 1 – MAX Job

Select:

```text
Job Type: MAX
```

Enter the following into Input Data:

```text
10, 25, 7, 99, 42, 3, 61
```

Click:

```text
SUBMIT JOB
```

Expected result:

```text
99
```

Example GUI output:

```text
>> SUBMITTED   MAX   JOB: <job-id>
<< COMPLETED   <job-id>   |   RESULT: 99
   Combined 2 worker result(s)
```

The worker terminals should also show that the coordinator distributed chunks between the active workers.

---

# TEST 2 – PRIMECOUNT Job

Select:

```text
Job Type: PRIMECOUNT
```

Enter:

```text
2, 3, 4, 5, 6, 7, 8, 9, 10, 11
```

Click:

```text
SUBMIT JOB
```

The prime numbers are:

```text
2, 3, 5, 7, 11
```

Expected result:

```text
5
```

Example GUI output:

```text
>> SUBMITTED   PRIMECOUNT   JOB: <job-id>
<< COMPLETED   <job-id>   |   RESULT: 5
   Combined 2 worker result(s)
```

---

# TEST 3 – PRIMESUM Job

Select:

```text
Job Type: PRIMESUM
```

Set:

```text
Range Start: 1
Range End: 10
```

For PRIMESUM, the range fields are used instead of the Input Data list.

Click:

```text
SUBMIT JOB
```

The prime numbers between 1 and 10 are:

```text
2, 3, 5, 7
```

Expected result:

```text
17
```

Example GUI output:

```text
>> SUBMITTED   PRIMESUM   JOB: <job-id>
<< COMPLETED   <job-id>   |   RESULT: 17
   Combined 2 worker result(s)
```

---

# TEST 4 – CSV Input

The GUI supports loading numerical input from a CSV file.

Create a file called:

```text
test.csv
```

Place the following data inside the file:

```text
12,45,8,103,27,66,91,4
```

Save the file.

In the Client GUI:

1. Select `MAX`.
2. Click `LOAD CSV`.
3. Select `test.csv`.
4. Confirm the numbers appear in the Input Data area.
5. Click `SUBMIT JOB`.

Expected result:

```text
103
```

The same CSV loading feature can also be used for `PRIMECOUNT`.

---

# TEST 5 – Distributed Workload Processing

Keep both Worker 1 and Worker 2 running.

Submit any supported job from the Client GUI.

For example:

```text
MAX
10, 25, 7, 99, 42, 3, 61
```

Observe the coordinator terminal.

The coordinator should divide the computational workload between the available workers.

Example:

```text
Coordinator 1 assigned chunk 1 to worker 1
Coordinator 1 assigned chunk 2 to worker 2
```

The GUI should report:

```text
Combined 2 worker result(s)
```

This confirms that the result was produced using distributed processing rather than only one worker.

---

# TEST 6 – Concurrent Job Submission

The client supports multiple concurrent jobs.

To demonstrate this, use a larger PRIMESUM range so that processing does not finish immediately.

For example:

```text
Job Type: PRIMESUM
Range Start: 1
Range End: 2000000
```

Click:

```text
SUBMIT JOB
```

Immediately submit another job before the first job finishes.

For example:

```text
PRIMESUM
Range Start: 1
Range End: 1000000
```

The Results panel can show multiple submitted jobs identified by different Job IDs.

Example:

```text
>> SUBMITTED   PRIMESUM   JOB: <job-id-1>
>> SUBMITTED   PRIMESUM   JOB: <job-id-2>
<< COMPLETED   <job-id-2>   |   RESULT: ...
<< COMPLETED   <job-id-1>   |   RESULT: ...
```

The exact completion order may vary because jobs are processed concurrently.

---

# TEST 7 – Multiple Client Processes

The system supports multiple clients running simultaneously.

Keep the first Client GUI open.

Open another Command Prompt in the project directory and run:

```bat
java -cp build\classes fj.usp.cs324.distrilab.client.ClientGui
```

A second Client GUI should open.

You can now submit a job from Client 1 and another job from Client 2.

For example:

Client 1:

```text
MAX
10, 25, 7, 99, 42, 3, 61
```

Client 2:

```text
PRIMECOUNT
2, 3, 4, 5, 6, 7, 8, 9, 10, 11
```

Both clients should communicate with the distributed worker system and receive their results independently.

---

# TEST 8 – Leader Election

Leader election occurs when there is no active coordinator.

When the first worker starts, an election can begin automatically.

Example worker output:

```text
Worker 1 started election <election-id>
Worker 1 processing ELECTION <election-id>
Worker 1 accepted COORDINATOR 1 for election <election-id>
```

When Worker 2 joins, it receives information about the current coordinator.

Example:

```text
Worker 2 connected to worker 1
Worker 2 accepted COORDINATOR 1 for election <election-id>
```

The election selects a coordinator according to the Job Allocation Counter (JAC).

The worker with the lowest JAC is preferred.

If multiple workers have the same lowest JAC, the worker with the highest worker ID is selected.

---

# TEST 9 – Five-Job Coordinator Rotation

A coordinator can serve for a maximum of five assigned jobs during a term.

To test this:

1. Keep Bootstrap Server running.
2. Keep Worker 1 running.
3. Keep Worker 2 running.
4. Keep the Client GUI running.
5. Submit jobs until the current coordinator reaches five jobs.

The jobs can be any combination of:

```text
MAX
PRIMECOUNT
PRIMESUM
```

Observe the coordinator terminal.

After the fifth job, output should indicate that the current term has ended and a new election is beginning.

Example:

```text
Coordinator 1 completed five jobs; beginning a new election
Worker 1 started election <new-election-id>
```

The workers should then process the new election and agree on a coordinator.

Example:

```text
Worker 2 accepted COORDINATOR 2 for election <new-election-id>
```

---

# TEST 10 – Processing After Coordinator Change

After the new coordinator has been elected, submit another job from the GUI.

For example:

```text
MAX
20, 40, 80, 15, 100
```

Expected result:

```text
100
```

Observe the newly elected coordinator terminal.

It should accept the new job and distribute the workload.

Example:

```text
Coordinator 2 accepted job <job-id>
Coordinator 2 assigned chunk 1 to worker 1
Coordinator 2 assigned chunk 2 to worker 2
```

This verifies that job processing continues after a coordinator change.

---

# TEST 11 – Three-Worker Distribution (Optional)

To demonstrate the system with more than two workers, start Worker 3:

```bat
java -cp build\classes fj.usp.cs324.distrilab.worker.WorkerServer 3 1103 localhost 1099 localhost
```

Then submit a job from the Client GUI.

The coordinator should divide the workload across the available workers as evenly as possible.

The number of combined worker results shown by the client should reflect the workers used for the job.

---

# Supported Jobs

## MAX

Returns the largest value from an unsorted list of integers.

Example:

```text
MAX(10, 25, 7, 99, 42, 3, 61)
```

Result:

```text
99
```

## PRIMECOUNT

Counts the number of prime values in an unsorted list.

Example:

```text
PRIMECOUNT(2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
```

Result:

```text
5
```

## PRIMESUM

Calculates the sum of all prime numbers within a specified range.

Example:

```text
PRIMESUM(1, 10)
```

Result:

```text
17
```

---

# Distributed Processing

The coordinator receives computational jobs from clients.

For each submitted job, the coordinator divides the workload between available workers.

With two workers, output may include:

```text
Coordinator 1 assigned chunk 1 to worker 1
Coordinator 1 assigned chunk 2 to worker 2
```

The partial results returned by workers are combined to produce the final result.

Workers support concurrent job execution using Java threads.

---

# Leader Election Rules

The distributed system uses a custom leader election mechanism.

The election follows these rules:

- An ELECTION message is propagated through the worker network.
- Duplicate election messages are not processed repeatedly.
- Reachable active workers participate in the election.
- The worker with the lowest Job Allocation Counter (JAC) is preferred.
- If multiple workers have the same lowest JAC, the worker with the highest worker ID is selected.
- Each worker contains the variable `leaderman` with the value `cs324`.
- The elected coordinator is announced using a COORDINATOR message.
- Workers eventually agree on a coordinator for the election term.
- A coordinator may assign up to five jobs before a new election begins.

---

# Client Features

The DistriLab Client provides:

- Graphical user interface
- Manual numerical input
- CSV file input
- MAX jobs
- PRIMECOUNT jobs
- PRIMESUM jobs
- Concurrent job submissions
- Unique job IDs
- Completed job results
- Distributed worker result information
- Support for multiple Client GUI processes

---

# Quick Demo Commands

Assuming Command Prompt is already inside the project directory:

## Terminal 1 – Bootstrap

```bat
java -cp build\classes fj.usp.cs324.distrilab.bootstrap.BootstrapServer
```

## Terminal 2 – Worker 1

```bat
java -cp build\classes fj.usp.cs324.distrilab.worker.WorkerServer 1 1101 localhost 1099 localhost
```

## Terminal 3 – Worker 2

```bat
java -cp build\classes fj.usp.cs324.distrilab.worker.WorkerServer 2 1102 localhost 1099 localhost
```

## Terminal 4 – Client

```bat
java -cp build\classes fj.usp.cs324.distrilab.client.ClientGui
```

## Terminal 5 – Second Client (if demonstrating multiple clients)

```bat
java -cp build\classes fj.usp.cs324.distrilab.client.ClientGui
```

## Optional Worker 3

```bat
java -cp build\classes fj.usp.cs324.distrilab.worker.WorkerServer 3 1103 localhost 1099 localhost
```

---

# Recommended Demo Sequence

For the demonstration, the following sequence can be used:

```text
1. Build the project
2. Start Bootstrap Server
3. Start Worker 1
4. Start Worker 2
5. Show coordinator election
6. Start Client GUI
7. Test MAX → expected 99
8. Test PRIMECOUNT → expected 5
9. Test PRIMESUM(1,10) → expected 17
10. Test CSV loading → expected MAX 103
11. Show workload being divided between Worker 1 and Worker 2
12. Submit concurrent jobs
13. Open a second Client GUI and demonstrate multiple clients
14. Submit enough jobs to reach the five-job coordinator limit
15. Show the new leader election
16. Submit another job after the coordinator change
```

---

# Troubleshooting

## Port already in use

If an error indicates that port `1099`, `1101`, `1102`, or another worker port is already in use, an old Bootstrap or Worker process may still be running.

Close the old Java process/terminal before starting another process using the same port.

Do not run two workers using the same worker ID or port.

## Client cannot find a coordinator

Check that:

```text
Bootstrap Server is running
Worker 1 is running
Worker 2 is running
A coordinator has been elected
```

Then start or restart the Client GUI.

## Worker cannot connect

Confirm that the Bootstrap Server was started first and is listening on:

```text
localhost:1099
```

Also confirm that every worker uses a unique registry port.

---

# Team Contributions

The project was developed collaboratively using Git branches and pull requests.

- Kavish – Bootstrap and network components
- Aditya – Leader election components
- Atharv – Job processing and client components

Integration and testing were performed after merging the individual branches into `main`.

---

# Testing Completed

The integrated system was tested using multiple worker processes.

The following functionality was verified:

- Successful project compilation
- Bootstrap Server startup
- Worker registration
- Worker discovery
- Worker-to-worker connection
- Leader election
- Coordinator announcement
- MAX job processing
- PRIMECOUNT job processing
- PRIMESUM job processing
- CSV input
- Workload distribution between workers
- Concurrent job submission
- Coordinator term limit
- New election after five coordinator jobs
- Successful job processing after coordinator change

---

# Git Repository

Development was managed using Git with separate branches for the major system components.

Branches used during development included:

```text
main
kavish-bootstrap-network
aditya-leader-election
atharv-jobs-client
```

The completed integrated version of the system is available on the `main` branch.
