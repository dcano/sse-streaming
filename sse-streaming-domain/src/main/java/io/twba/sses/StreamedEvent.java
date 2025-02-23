package io.twba.sses;

import java.time.Instant;
import java.util.UUID;

record StreamedEvent(UUID id, EventPayload payload, Instant creationTime, DataDomain dataDomain, ProducerId producerId, long offset)  {
}
