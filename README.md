# SocialHub Client

In order for this to work, you'll need to specify the usual RabbitMQ credentials pointing to the broker connected to the SocialHub service itself. All messages are routed not through HTTP, but RabbitMQ. We still obtain a valid JWT token before issuing the request, as that token will be used on the service-side to validate that the request is coming from a well-known user in the Spring Authorization Server [instance running here](https://authorization.joshlong.com).

You'll also need to know the name of a valid OAuth client and client secret for the aforementioned Spring Authorization Server instance. So, communication is doubly secure: everything is conducted over TLS-secured AMQP, with a token that validates that the client is actually a well-known entity (or at least that they're using a real passsword that was easily guessed!).

