package com.joshlong.socialhubclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.client.RestTemplate;

/**
 * ideal for multi-tenant situations where each user migtht have a differently configured instance
 */
public class AuthenticatedSocialHub extends SocialHub {

    private final String clientId, clientSecret;

    public AuthenticatedSocialHub(String clientId, String clientSecret,
                                  MessageChannel requests, MessageChannel errors, RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(requests, errors, restTemplate, objectMapper);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public void post(Post post) throws Exception {
        var token = this.authenticate(this.clientId, this.clientSecret);
        this.post(token, post);
    }

}
