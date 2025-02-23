package io.twba.sses.shared;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Named
public class QueryBus {

    private final Map<String, SseQueryHandler<Query>> handlersMap;

    @Inject
    public QueryBus(List<SseQueryHandler<Query>> handlers) {
        handlersMap = Map.of();
        handlers.forEach(handler -> this.handlersMap.put(handler.handles(), handler));
    }

    public <T extends Query> Publisher<StreamedCloudEvent> execute(T query) {
        if(!handlersMap.containsKey(query.getClass().getName())) {
            return Flux.error(new IllegalStateException("No handler found for query: " + query.getClass().getName()));
        }
        return Flux.from(handlersMap.get(query.getClass().getName()).execute(query));
    }

}
