package io.twba.sses;

import io.cloudevents.core.v1.CloudEventBuilder;
import io.twba.sses.shared.StreamedCloudEvent;
import jakarta.inject.Named;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;

@Named
class CloudEventMapper implements Function<StreamedEvent, StreamedCloudEvent> {

    @Override
    public StreamedCloudEvent apply(StreamedEvent streamedEvent) {

        CloudEventBuilder cloudEventBuilder = new CloudEventBuilder()
                .withId(streamedEvent.id().toString())
                .withType(streamedEvent.payload().type())
                .withSource(URI.create("twba:sse-streaming:" + streamedEvent.dataDomain().domain() + ":" + streamedEvent.producerId().producer()))
                .withExtension(StreamedCloudEvent.CLOUD_EVENT_TIMESTAMP, streamedEvent.creationTime().toEpochMilli())
                .withExtension(StreamedCloudEvent.CLOUD_EVENT_DATA_DOMAIN, streamedEvent.dataDomain().domain())
                .withExtension(StreamedCloudEvent.CLOUD_EVENT_GENERATING_APP_NAME, streamedEvent.producerId().producer())
                .withExtension(StreamedCloudEvent.CLOUD_EVENT_EVENT_OFFSET, streamedEvent.offset())
                .withData("application/json", streamedEvent.payload().content().getBytes(StandardCharsets.UTF_8));

        if(Objects.nonNull(streamedEvent.payload().referenceId()) && !streamedEvent.payload().referenceId().isEmpty()) {
            cloudEventBuilder.withSubject(streamedEvent.payload().referenceId());
        }

        return StreamedCloudEvent.from(cloudEventBuilder.build());
    }
}
