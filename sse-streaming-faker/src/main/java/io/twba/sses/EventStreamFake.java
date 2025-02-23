package io.twba.sses;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

class EventStreamFake implements EventStream {

    @Override
    public void append(StoredEvent storedEvent) {

    }

    @Override
    public Publisher<StreamedEvent> retrieve(String dataDomain, String consumerId, long partition, long offset) {
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(this::randomEvent);
    }

    private Flux<StreamedEvent> randomEvent(long n) {
        return Flux.just(new StreamedEvent(UUID.randomUUID(), new EventPayload("payload", "eventtype-" + n, UUID.randomUUID().toString()), Instant.now(), new DataDomain("dataDomainTest"), new ProducerId("producerId"),  n));
    }
}
