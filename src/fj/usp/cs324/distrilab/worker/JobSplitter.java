package fj.usp.cs324.distrilab.worker;

import fj.usp.cs324.distrilab.common.*;
import java.util.ArrayList;
import java.util.List;

public final class JobSplitter {
    private JobSplitter() { }

    public static List<JobChunk> split(JobRequest request, int requestedParts) {
        if (requestedParts < 1) throw new IllegalArgumentException("At least one worker is required");
        return request.type() == JobType.PRIMESUM
                ? splitRange(request, requestedParts)
                : splitList(request, requestedParts);
    }

    private static List<JobChunk> splitList(JobRequest request, int requestedParts) {
        if (request.numbers() == null || request.numbers().isEmpty()) {
            throw new IllegalArgumentException(request.type() + " requires at least one number");
        }
        int parts = Math.min(requestedParts, request.numbers().size());
        List<JobChunk> chunks = new ArrayList<>();
        int baseSize = request.numbers().size() / parts;
        int remainder = request.numbers().size() % parts;
        int index = 0;
        for (int part = 0; part < parts; part++) {
            int size = baseSize + (part < remainder ? 1 : 0);
            List<Long> slice = List.copyOf(request.numbers().subList(index, index + size));
            chunks.add(new JobChunk(request.jobId(), request.type(), slice, 0, 0, part + 1));
            index += size;
        }
        return chunks;
    }

    private static List<JobChunk> splitRange(JobRequest request, int requestedParts) {
        if (request.rangeStart() > request.rangeEnd()) {
            throw new IllegalArgumentException("Range start must not exceed range end");
        }
        long length = request.rangeEnd() - request.rangeStart() + 1;
        int parts = (int) Math.min(requestedParts, length);
        long baseSize = length / parts;
        long remainder = length % parts;
        long start = request.rangeStart();
        List<JobChunk> chunks = new ArrayList<>();
        for (int part = 0; part < parts; part++) {
            long size = baseSize + (part < remainder ? 1 : 0);
            long end = start + size - 1;
            chunks.add(new JobChunk(request.jobId(), request.type(), List.of(), start, end, part + 1));
            start = end + 1;
        }
        return chunks;
    }
}
