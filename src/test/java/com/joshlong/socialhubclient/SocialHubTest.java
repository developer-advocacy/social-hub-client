package com.joshlong.socialhubclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

class SocialHubTest {

    private final MessageChannel request = MessageChannels.direct().getObject();

    private final MessageChannel errors = MessageChannels.direct().getObject();

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SocialHub socialHub = new SocialHub(this.request, this.errors, this.restTemplate, this.objectMapper);

    @Test
    void postTest() throws Exception {
        var content = "hello, world @ " + Instant.now();
        var subscribableChannel = (SubscribableChannel) this.request;
        subscribableChannel.subscribe(message -> {
            Assertions.assertTrue(message.getHeaders().containsKey(HttpHeaders.AUTHORIZATION), "there needs to be an authorization header");
            var json = (String) message.getPayload();
            try {
                var jn = this.objectMapper.readTree(json);
                var contentFromJson = jn.get("content").asText();
                Assertions.assertEquals(content, contentFromJson);
            }//
            catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        this.socialHub.post("123", new SocialHub.Post("twitter".split(","), content, new SocialHub.MediaResource[0]));
    }
}
