package com.pdunghh.shared.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID userId,
        String username,
        String email,
        Instant createdAt
) implements Serializable {
}
