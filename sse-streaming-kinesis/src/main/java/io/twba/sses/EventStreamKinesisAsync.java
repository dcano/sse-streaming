package io.twba.sses;

import org.reactivestreams.Publisher;

class EventStreamKinesisAsync implements EventStream {

    EventStreamKinesisAsync() {

    }

    @Override
    public void append(StoredEvent storedEvent) {

    }

    @Override
    public Publisher<StreamedEvent> retrieve(String dataDomain, String consumerId, long partition, long offset) {
        return null;
    }



}
