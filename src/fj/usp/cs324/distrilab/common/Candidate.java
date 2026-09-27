package fj.usp.cs324.distrilab.common;

import java.io.Serializable;

public record Candidate(int workerId, int jac) implements Serializable {
    public static Candidate better(Candidate first, Candidate second) {
        if (first == null) return second;
        if (second == null) return first;
        if (first.jac != second.jac) return first.jac < second.jac ? first : second;
        return first.workerId >= second.workerId ? first : second;
    }
}
