package com.joshlong.socialhubclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;

@Configuration
public class SocialHubChannels {

    public static final String SOCIALHUB_REQUESTS_CHANNEL_NAME = "socialHubRequests";

    public static final String SOCIALHUB_ERRORS_CHANNEL_NAME = "socialHubErrors";

    @Bean(name = SOCIALHUB_REQUESTS_CHANNEL_NAME)
    public MessageChannel socialHubRequestsMessageChannel() {
        return MessageChannels.direct().getObject();
    }

    @Bean(name = SOCIALHUB_ERRORS_CHANNEL_NAME)
    public MessageChannel socialHubErrorsMessageChannel() {
        return MessageChannels.publishSubscribe().getObject();
    }
}
