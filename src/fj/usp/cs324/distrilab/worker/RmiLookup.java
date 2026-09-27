package fj.usp.cs324.distrilab.worker;

import fj.usp.cs324.distrilab.common.WorkerInfo;
import fj.usp.cs324.distrilab.remote.WorkerService;
import java.rmi.registry.LocateRegistry;

public final class RmiLookup {
    private RmiLookup() { }

    public static WorkerService worker(WorkerInfo info) throws Exception {
        return (WorkerService) LocateRegistry.getRegistry(info.host(), info.registryPort())
                .lookup(info.bindingName());
    }
}
