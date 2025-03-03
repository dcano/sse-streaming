package io.twba.sses.api;

import io.twba.sses.StreamAllEventsForDataDomainQuery;
import io.twba.sses.shared.QueryBus;
import io.twba.sses.shared.StreamedCloudEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("sse-streaming")
public class SseController {

    private static final long KEEP_ALIVE_INTERNAL = 500; //TODO extract to properties

    private final QueryBus queryBus;

    @Autowired
    public SseController(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @GetMapping(value = "/{dataDomain}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamedCloudEvent>> streamEvents(@RequestParam("consumer") String consumerApp, @PathVariable String dataDomain) {
        StreamAllEventsForDataDomainQuery query = new StreamAllEventsForDataDomainQuery(dataDomain, consumerApp);
        return Flux.from(queryBus.execute(query))
                .map(streamedEvent -> ServerSentEvent.builder(streamedEvent).build())
                .mergeWith(Flux.interval(Duration.ofMillis(KEEP_ALIVE_INTERNAL))
                        .map(i -> ServerSentEvent.<StreamedCloudEvent>builder().comment("keep-alive").build()))
                .onErrorStop();
    }

}
