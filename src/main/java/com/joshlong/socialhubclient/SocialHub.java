package com.joshlong.socialhubclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/***
 * TODO I'll develop this here for now but it needs to be a separate {@code jar} for easier and true end-to-end testing
 *
 * this is the client that we'll distribute to talk to the SocialHub bus.
 *
 * @author Josh Long
 */
public class SocialHub {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final MessageChannel channel, errors;

    private static void notNull(Object o, String description) {
        Assert.notNull(o, "the %s should not be null".formatted(description));
    }

    public SocialHub(MessageChannel requests, MessageChannel errors, RestTemplate restTemplate, ObjectMapper objectMapper) {

        notNull(requests, "requests channel");
        notNull(errors, "errors channel");
        notNull(restTemplate, "restTemplate");
        notNull(objectMapper, "objectMapper");

        this.channel = requests;
        this.restTemplate = restTemplate;
        this.errors = errors;
        this.objectMapper = objectMapper;
    }

    public record Post(String[] platforms, String content, MediaResource[] media) { }

    public record MediaResource(MediaType mediaType, Resource resource) {  }

    public enum MediaType {

        JPG(org.springframework.http.MediaType.IMAGE_JPEG_VALUE),

        PNG(org.springframework.http.MediaType.IMAGE_PNG_VALUE);

        private final String contentType;

        MediaType(String contentType) {
            this.contentType = contentType;
        }

        public String contentType() {
            return this.contentType;
        }
    }

    /**
     * Returns an access token given a proper pair of client-credentials
     * @param clientId the client ID
     * @param clientSecret the client secret
     * @return an OAuth access token
     */
    public String authenticate(String clientId, String clientSecret) {
        var headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "user.read");

        var entity = new HttpEntity<>(body, headers);
        var url = "https://authorization.joshlong.com/oauth2/token";
        var response = this.restTemplate.postForEntity(url, entity, JsonNode.class);
        Assert.isTrue(response.getStatusCode().is2xxSuccessful(), "the HTTP request must return HTTP status code 200");
        var jn = response.getBody();
        return jn.get("access_token").asText();
    }

    /**
     *
     * @param token an OAuth access token. See {@link this#authenticate(String, String)} to obtain a valid token.
     * @param post  the post to submit
     * @throws Exception it's the internet. Anything could go wrong.
     */
    public void post(String token, Post post) throws Exception {
        // todo some sort of encoding for the token?
        var json = this.encodePostAsJson(post);
        var message = MessageBuilder//
                .withPayload(json)//
                .copyHeadersIfAbsent(Map.of(HttpHeaders.AUTHORIZATION, token))//
                .setErrorChannel(this.errors)//
                .build();
        this.channel.send(message);
    }

    private JsonNode buildMediaSourceJson(MediaResource resource) throws Exception {
        var node = this.objectMapper.createObjectNode();
        var contentType = resource.mediaType().contentType();

        if (resource.resource() instanceof Base64ByteArrayResource base64ByteArrayResource) {
            node.put("name", base64ByteArrayResource.name());
            node.put("content", base64ByteArrayResource.base64EncodedContent());
        } //
        else {
            node.put("name", UUID.randomUUID().toString());
            node.put("content", Base64.getEncoder().encode(resource.resource().getContentAsByteArray()));
        }
        node.put("content-type", contentType);
        return node;
    }


    private String encodePostAsJson(Post post) throws Exception {
        var postRequestNode = this.objectMapper.createObjectNode();
        var platformsNode = postRequestNode.putArray("platforms");
        for (var platform : post.platforms()) {
            platformsNode.add(platform);
        }
        postRequestNode.put("content", post.content());
        var mediaNode = postRequestNode.putArray("media");
        for (var resource : post.media())
            mediaNode.add(buildMediaSourceJson(resource));
        return postRequestNode.toString();
    }

    private static class Base64ByteArrayResource extends ByteArrayResource {

        private final String name, base64EncodedContent;

        Base64ByteArrayResource(String name, InputStream inputStream) throws IOException {
            super(inputStream.readAllBytes());
            this.base64EncodedContent = Base64.getEncoder().encodeToString(getContentAsByteArray());
            this.name = name;
            Assert.notNull(this.name, "you must specify a name");
            Assert.notNull(this.base64EncodedContent, "you must specify valid, non-null content");
        }

        public String base64EncodedContent() {
            return this.base64EncodedContent;
        }

        public String name() {
            return this.name;
        }

    }

}
