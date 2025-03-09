package io.twba.sses;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.*;
import software.amazon.awssdk.services.kinesis.model.Record;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

class EventStreamKinesisSync implements EventStream {

    //arn:aws:kinesis:<AWS_REGION>:<AWS_ACCOUNT_ID>:stream/I<STREAM_NAME>
    private static final Logger LOG = LoggerFactory.getLogger(EventStreamKinesisSync.class);
    private static final String KINESIS_ARN_TEMPLATE = "arn:aws:kinesis:%s:%s:stream/%s";
    private final KinesisClient kinesisClient;
    private final AwsProperties awsProperties;
    private final Sinks.Many<StreamedEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

    EventStreamKinesisSync(AwsCredentialsProvider awsCredentialsProvider, AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
        kinesisClient = KinesisClient.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(Region.of(awsProperties.getRegion()))
                .build();
    }

    @Override
    public void append(StoredEvent storedEvent) {

    }

    @Override
    public Publisher<StreamedEvent> retrieve(String dataDomain, String consumerId, long partition, long offset) {
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(n ->pollKinesisStream(n, dataDomain));
    }

    private Flux<StreamedEvent> pollKinesisStream(long n, String dataDomain) {

        return Flux.create(sink -> {
            try {

                getShardIterators(dataDomain).forEach(shardIterator -> {
                    GetRecordsRequest getRecordsRequest = GetRecordsRequest.builder()
                            .shardIterator(shardIterator)
                            .limit(10)
                            .build();

                    GetRecordsResponse recordsResponse = kinesisClient.getRecords(getRecordsRequest);

                    List<Record> records = recordsResponse.records();
                    for (Record record : records) {
                        String data = StandardCharsets.UTF_8.decode(record.data().asByteBuffer()).toString();
                        StreamedEvent streamedEvent = new StreamedEvent(UUID.randomUUID(), new EventPayload(data, "type_test" + n, "reference_id_test" + n), Instant.now(), new DataDomain(dataDomain), new ProducerId("producer-test" + n), n);
                        sink.next(streamedEvent); // Emit to Flux
                    }
                });


            } catch (Exception e) {
                LOG.error("Error retrieving records from Kinesis", e);
                sink.error(e);
            }
        });

    }

    private Stream<String> getShardIterators(String dataDomain) {
        ListShardsRequest listShardsRequest = ListShardsRequest.builder().streamName(streamNameOf(dataDomain)).build();
        ListShardsResponse listShardsResponse = kinesisClient.listShards(listShardsRequest);

        if (!listShardsResponse.shards().isEmpty()) {
            return listShardsResponse.shards().stream()
                    .map(shard -> GetShardIteratorRequest.builder()
                            .streamName(streamNameOf(dataDomain))
                            .shardId(shard.shardId())
                            .shardIteratorType(ShardIteratorType.LATEST)
                            .build())
                    .map(shardRequest -> kinesisClient.getShardIterator(shardRequest).shardIterator());

        }
        return Stream.empty();
    }

    private String streamNameOf(String dataDomain) {
        return String.format(KINESIS_ARN_TEMPLATE, awsProperties.getRegion(), awsProperties.getAccountId(), dataDomain); // kinesisProperties.getStreamNamePrefix() + dataDomain;
    }

}
