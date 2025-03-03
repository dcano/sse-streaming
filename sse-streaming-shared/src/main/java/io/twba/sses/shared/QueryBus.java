package io.twba.sses.shared;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
@Named
public class QueryBus {

    private final Map<String, SseQueryHandler> handlersMap;

    @Inject
    public QueryBus(List<SseQueryHandler> handlers) {
        handlersMap = new HashMap<>();
        handlers.forEach(handler -> this.handlersMap.put(handler.handles(), handler));
    }

    public <T extends Query> Publisher<StreamedCloudEvent> execute(T query) {
        if(!handlersMap.containsKey(query.getClass().getName())) {
            return Flux.error(new IllegalStateException("No handler found for query: " + query.getClass().getName()));
        }
        return Flux.from(handlersMap.get(query.getClass().getName()).execute(query));
    }

}
