package io.twba.sses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Producer;
import jakarta.validation.constraints.NotNull;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;


class EventStreamRabbitMQ implements EventStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventStreamRabbitMQ.class);
    private static final String STREAM_PREFIX = "stream.";
    private final Environment environment;
    private final ObjectMapper objectMapper;

    public EventStreamRabbitMQ(RabbitStreamProperties properties) {

        this.environment = Environment.builder()
                .port(properties.getPort())
                .host(properties.getHost())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .build();
        this.objectMapper = setUpObjectMapper();
    }

    private static ObjectMapper setUpObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Override
    public void append(StoredEvent storedEvent) {

        ensureStream(storedEvent.dataDomain());
        Producer producer = getProducer(storedEvent.dataDomain());

        try{
            producer.send(
                    producer.messageBuilder()
                            .properties()
                            .creationTime(storedEvent.creationTime().toEpochMilli())
                            .messageBuilder()
                            .addData(objectMapper.writeValueAsString(storedEvent).getBytes(StandardCharsets.UTF_8))
                            .build(),
                    confirmationStatus -> {
                        if(!confirmationStatus.isConfirmed()) {
                            LOGGER.error("Failed to confirm event: {}", storedEvent);
                            throw new IllegalStateException(String.format("Unable to confirm event %s for storage", storedEvent.payload().type()));
                        }
                    });
        } catch(JsonProcessingException e) {
            throw new IllegalStateException(String.format("Unable to serialize event %s for storage", storedEvent.payload().type()), e);
        }
    }


    @Override
    public Publisher<StreamedEvent> retrieve(String dataDomain, String consumerId, long partition, long offset) {
        return null;
    }

    private void ensureStream(@NotNull DataDomain dataDomain) {

        if(!environment.streamExists(dataDomain.domain())) {
            environment.streamCreator()
                    .stream(streamOf(dataDomain.domain()))
                    .create();
        }

    }

    private Producer getProducer(DataDomain dataDomain) {
        return environment.producerBuilder()
                .stream(streamOf(dataDomain.domain()))
                .build();
    }

    private static String streamOf(String domain) {
        return STREAM_PREFIX + domain;
    }

}
