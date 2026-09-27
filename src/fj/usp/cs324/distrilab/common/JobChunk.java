package fj.usp.cs324.distrilab.common;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record JobChunk(UUID jobId, JobType type, List<Long> numbers,
                       long rangeStart, long rangeEnd, int chunkNumber)
        implements Serializable {
}
