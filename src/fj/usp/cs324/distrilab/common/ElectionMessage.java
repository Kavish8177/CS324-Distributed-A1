package fj.usp.cs324.distrilab.common;

import java.io.Serializable;
import java.util.UUID;

public record ElectionMessage(UUID electionId, int initiatorId, int senderId)
        implements Serializable {
}
