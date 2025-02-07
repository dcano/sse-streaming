package io.twba.sses;

import org.reactivestreams.Publisher;

interface EventStream {

    void append(StoredEvent storedEvent);
    Publisher<StreamedEvent> retrieve(String dataDomain, String consumerId, long partition, long offset);
}
