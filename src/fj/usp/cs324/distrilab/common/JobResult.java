package fj.usp.cs324.distrilab.common;

import java.io.Serializable;
import java.util.UUID;

public record JobResult(UUID jobId, JobType type, long value, String explanation)
        implements Serializable {
}
