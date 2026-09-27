package fj.usp.cs324.distrilab.common;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record JobRequest(UUID jobId, JobType type, List<Long> numbers,
                         long rangeStart, long rangeEnd) implements Serializable {
    public static JobRequest max(List<Long> numbers) {
        return new JobRequest(UUID.randomUUID(), JobType.MAX, List.copyOf(numbers), 0, 0);
    }

    public static JobRequest primeCount(List<Long> numbers) {
        return new JobRequest(UUID.randomUUID(), JobType.PRIMECOUNT, List.copyOf(numbers), 0, 0);
    }

    public static JobRequest primeSum(long start, long end) {
        return new JobRequest(UUID.randomUUID(), JobType.PRIMESUM, List.of(), start, end);
    }
}
