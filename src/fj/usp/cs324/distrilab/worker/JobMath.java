package fj.usp.cs324.distrilab.worker;

import fj.usp.cs324.distrilab.common.*;
import java.util.List;

public final class JobMath {
    private JobMath() { }

    public static JobResult calculate(JobChunk chunk) {
        long value = switch (chunk.type()) {
            case MAX -> chunk.numbers().stream().mapToLong(Long::longValue).max()
                    .orElseThrow(() -> new IllegalArgumentException("MAX chunk is empty"));
            case PRIMECOUNT -> chunk.numbers().stream().filter(JobMath::isPrime).count();
            case PRIMESUM -> {
                long sum = 0;
                for (long number = Math.max(2, chunk.rangeStart()); number <= chunk.rangeEnd(); number++) {
                    if (isPrime(number)) sum += number;
                }
                yield sum;
            }
        };
        return new JobResult(chunk.jobId(), chunk.type(), value,
                "Chunk " + chunk.chunkNumber() + " completed");
    }

    public static long combine(JobType type, List<JobResult> results) {
        if (results.isEmpty()) throw new IllegalArgumentException("No chunk results received");
        return switch (type) {
            case MAX -> results.stream().mapToLong(JobResult::value).max().orElseThrow();
            case PRIMECOUNT, PRIMESUM -> results.stream().mapToLong(JobResult::value).sum();
        };
    }

    public static boolean isPrime(long value) {
        if (value < 2) return false;
        if (value == 2) return true;
        if (value % 2 == 0) return false;
        for (long divisor = 3; divisor <= value / divisor; divisor += 2) {
            if (value % divisor == 0) return false;
        }
        return true;
    }
}
