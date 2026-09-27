package fj.usp.cs324.distrilab.common;

import java.io.Serializable;

public record WorkerInfo(int id, String host, int registryPort, int jac)
        implements Serializable {
    public String bindingName() {
        return "Worker-" + id;
    }
}
