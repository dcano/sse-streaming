package io.twba.sses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@EnableConfigurationProperties
@Configuration
public class EventStreamDevConfig {

    @Bean
    EventStream eventStream(@Autowired AwsProperties awsProperties, @Autowired AwsCredentialsProvider awsCredentialsProvider) {
        return new EventStreamKinesisSync(awsCredentialsProvider, awsProperties);
    }

    @Bean
    AwsCredentialsProvider basicCredentialsProvider(AwsProperties awsProperties) {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(awsProperties.getAccessKeyId(), awsProperties.getSecretAccessKey()));
    }
    
    @ConfigurationProperties(prefix = "twba.aws")
    @Bean
    AwsProperties awsProperties() {
        return new AwsProperties();
    }

}
