package fj.usp.cs324.distrilab.remote;

import fj.usp.cs324.distrilab.common.WorkerInfo;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BootstrapService extends Remote {
    WorkerInfo registerWorker(WorkerInfo worker) throws RemoteException;
    void updateWorker(WorkerInfo worker) throws RemoteException;
    void unregisterWorker(int workerId) throws RemoteException;
    List<WorkerInfo> getActiveWorkers() throws RemoteException;
}
