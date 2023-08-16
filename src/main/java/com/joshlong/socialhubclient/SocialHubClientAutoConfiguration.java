package com.joshlong.socialhubclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@Import(SocialHubChannels.class)
class SocialHubClientAutoConfiguration {

    private final String amqpDestination = "socialhub-requests";

    @Bean
    IntegrationFlow outboundAmqpAdapterFlow(SocialHubChannels socialHubChannels, AmqpTemplate amqpTemplate) {
        var amqpOutboundAdapter = Amqp//
                .outboundAdapter(amqpTemplate)//
                .exchangeName(this.amqpDestination)//
                .routingKey(this.amqpDestination);
        return IntegrationFlow//
                .from(socialHubChannels.socialHubRequestsMessageChannel())//
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
    SocialHub socialHub(SocialHubChannels socialHubChannels, ObjectMapper objectMapper, RestTemplate restTemplate) {
        return new SocialHub(
                socialHubChannels.socialHubRequestsMessageChannel(),//
                socialHubChannels.socialHubErrorsMessageChannel(),//
                restTemplate,//
                objectMapper//
        );
    }
}

