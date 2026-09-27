package fj.usp.cs324.distrilab.common;

import java.io.Serializable;
import java.util.UUID;

public record CoordinatorMessage(UUID electionId, int coordinatorId, int senderId)
        implements Serializable {
}
