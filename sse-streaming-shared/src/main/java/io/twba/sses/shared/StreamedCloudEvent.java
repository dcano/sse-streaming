package io.twba.sses.shared;

import io.cloudevents.CloudEvent;

public record StreamedCloudEvent(CloudEvent cloudEvent) {

    public static final String CLOUD_EVENT_TIMESTAMP = "eventepochtimestamp";
    public static final String CLOUD_EVENT_GENERATING_APP_NAME = "producerid";
    public static final String CLOUD_EVENT_DATA_DOMAIN = "datadomain";
    public static final String CLOUD_EVENT_EVENT_OFFSET = "offset";

    public static StreamedCloudEvent from(CloudEvent cloudEvent) {
        return new StreamedCloudEvent(cloudEvent);
    }

}
