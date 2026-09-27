package fj.usp.cs324.distrilab.remote;

import fj.usp.cs324.distrilab.common.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WorkerService extends Remote {
    WorkerInfo getInfo() throws RemoteException;
    void addNeighbour(WorkerInfo worker) throws RemoteException;
    void removeNeighbour(int workerId) throws RemoteException;
    Candidate receiveElection(ElectionMessage message) throws RemoteException;
    void receiveCoordinator(CoordinatorMessage message) throws RemoteException;
    void startElection() throws RemoteException;
    int getCoordinatorId() throws RemoteException;
    JobResult submitJob(JobRequest request) throws RemoteException;
    JobResult executeChunk(JobChunk chunk) throws RemoteException;
}
