package com.joshlong.socialhubclient;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;

@SpringBootApplication
class TestSocialHubClientApplication {

    public static void main(String[] args) {
        Map
                .of(
                        "spring.rabbitmq.host", "127.0.0.1",//
                        "spring.rabbitmq.username", "user",//
                        "spring.rabbitmq.password", "password",//
                        "spring.rabbitmq.port", "5672"//
                )
                .forEach(System::setProperty);
        SpringApplication.run(TestSocialHubClientApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(SocialHub socialHub) {
        return args -> {
            var clientId = System.getenv("SOCIALHUB_JOSHLONG_CLIENT_KEY");
            var clientSecret = System.getenv("SOCIALHUB_JOSHLONG_CLIENT_KEY_SECRET");
            Assert.notNull(clientId, "the clientId must be non-null");
            Assert.notNull(clientSecret, "the clientSecret must be non-null");
            var token = socialHub.authenticate(clientId, clientSecret);
            var mediaResources = new SocialHub.MediaResource[]{
                    new SocialHub.MediaResource(SocialHub.MediaType.PNG, new ClassPathResource("/sample.png"))
            };
            var post = new SocialHub.Post("twitter".split(","), "Hello, world, what do you think of my image (" + Instant.now() + ")?", mediaResources);
            socialHub.post(token, post);
        };
    }

}
