package io.twba.sses;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

record StoredEvent(@NotNull UUID id,
                   @NotNull EventPayload payload,
                   @NotNull Instant creationTime,
                   @NotNull DataDomain dataDomain,
                   String partitionKey,
                   @NotNull String correlationId) {
}
