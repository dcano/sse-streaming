package io.twba.sses;

import lombok.Data;

@Data
public class RabbitStreamProperties {

    private String host;
    private int port;
    private String username;
    private String password;

}
