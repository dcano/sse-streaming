package io.twba.sses;

import io.twba.sses.shared.SseQueryHandler;
import io.twba.sses.shared.StreamedCloudEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Named
class StreamAllEventsForDataDomainQueryHandler implements SseQueryHandler<StreamAllEventsForDataDomainQuery> {

    private final EventStream eventStream;
    private final CloudEventMapper cloudEventMapper;

    @Inject
    StreamAllEventsForDataDomainQueryHandler(EventStream eventStream, CloudEventMapper cloudEventMapper) {
        this.eventStream = eventStream;
        this.cloudEventMapper = cloudEventMapper;
    }

    @Override
    public Publisher<StreamedCloudEvent> execute(StreamAllEventsForDataDomainQuery allEventsQuery) {
        return Flux.from(eventStream.retrieve(allEventsQuery.dataDomain(), allEventsQuery.consumerId(), -1L, -1L))
                .map(cloudEventMapper);
    }
}
