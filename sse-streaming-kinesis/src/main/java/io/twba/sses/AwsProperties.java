package io.twba.sses;

import lombok.Data;

@Data
public class AwsProperties {

    private String accessKeyId;
    private String secretAccessKey;
    private String accountId;
    private String region;

}
