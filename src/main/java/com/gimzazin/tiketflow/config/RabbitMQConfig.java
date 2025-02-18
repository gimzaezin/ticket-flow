package com.gimzazin.tiketflow.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private final Dotenv dotenv = Dotenv.load();

    @Bean
    public ConnectionFactory rabbitConnectionFactory() {
        String host = dotenv.get("RABBITMQ_HOST", "localhost");
        int port = Integer.parseInt(dotenv.get("RABBITMQ_PORT", "5672"));
        String username = dotenv.get("RABBITMQ_USERNAME", "guest");
        String password = dotenv.get("RABBITMQ_PASSWORD", "guest");

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public TopicExchange exchange() {
        String exchangeName = dotenv.get("RABBITMQ_EXCHANGE_NAME", "reservation.exchange");
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue queue() {
        String queueName = dotenv.get("RABBITMQ_QUEUE_NAME", "reservation.queue");
        return new Queue(queueName);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        String routingKey = dotenv.get("RABBITMQ_ROUTING_KEY", "reservation.routing");
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory rabbitConnectionFactory) {
        return new RabbitTemplate(rabbitConnectionFactory);
    }
}
