package com.joshlong.socialhubclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
class SocialHubClientAutoConfiguration {

    private final String amqpDestination = "socialhub-requests";

    public static final String SOCIALHUB_REQUESTS_CHANNEL_NAME = "socialHubRequests";

    public static final String SOCIALHUB_ERRORS_CHANNEL_NAME = "socialHubErrors";

    @Bean(name = SOCIALHUB_REQUESTS_CHANNEL_NAME)
    MessageChannel socialHubRequestsMessageChannel() {
        return MessageChannels.direct().getObject();
    }

    @Bean(name = SOCIALHUB_ERRORS_CHANNEL_NAME)
    MessageChannel socialHubErrorsMessageChannel() {
        return MessageChannels.publishSubscribe().getObject();
    }

    @Bean
    IntegrationFlow outboundAmqpAdapterFlow(AmqpTemplate amqpTemplate) {
        var amqpOutboundAdapter = Amqp//
                .outboundAdapter(amqpTemplate)//
                .exchangeName(this.amqpDestination)//
                .routingKey(this.amqpDestination);
        return IntegrationFlow//
                .from(this.socialHubRequestsMessageChannel())//
                .handle(amqpOutboundAdapter)//
                .get();
    }

    @Bean
    @ConditionalOnMissingBean(RestOperations.class)
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    SocialHub socialHub(ObjectMapper objectMapper, RestTemplate restTemplate) {
        return new SocialHub(
                this.socialHubRequestsMessageChannel(),//
                this.socialHubErrorsMessageChannel(),//
                restTemplate,//
                objectMapper//
        );
    }
}
