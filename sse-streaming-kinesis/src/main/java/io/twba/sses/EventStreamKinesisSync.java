package io.twba.sses;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

class EventStreamKinesisSync implements EventStream {

    private final KinesisClient kinesisClient;
    private final KinesisProperties kinesisProperties;
    private final Sinks.Many<StreamedEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

    EventStreamKinesisSync(KinesisProperties kinesisProperties, AwsCredentialsProvider awsCredentialsProvider) {
        this.kinesisProperties = kinesisProperties;
        kinesisClient = KinesisClient.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(Region.EU_CENTRAL_1)
                .build();
    }

    @Override
    public void append(StoredEvent storedEvent) {

    }

    @Override
    public Publisher<StreamedEvent> retrieve(String dataDomain, String consumerId, long partition, long offset) {
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(this::pollKinesisStream);
    }

    private Flux<StreamedEvent> pollKinesisStream(long n) {

        return Flux.just(new StreamedEvent(UUID.randomUUID(), new EventPayload("payload", "eventtype-" + n, UUID.randomUUID().toString()), Instant.now(), new DataDomain("dataDomainTest"), new ProducerId("producerId"),  n));
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
        return kinesisProperties.getStreamNamePrefix() + dataDomain;
    }

}
