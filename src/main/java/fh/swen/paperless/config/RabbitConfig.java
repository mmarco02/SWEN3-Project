package fh.swen.paperless.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * RabbitMQ topology: REST publishes document events to a topic exchange, every consumer
 * (currently only the OCR worker) has its own queue bound to it. Messages the worker cannot
 * process are moved to a dead letter queue instead of being redelivered forever.
 * <p>
 * Declared here and in the OCR worker, so it exists no matter which application starts first.
 * Nothing connects at startup, the declarations are sent with the first message.
 */
@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitConfig {

    @Bean
    public TopicExchange documentExchange(MessagingProperties properties) {
        return new TopicExchange(properties.exchange());
    }

    @Bean
    public Queue ocrQueue(MessagingProperties properties) {
        return QueueBuilder.durable(properties.ocrQueue())
                .deadLetterExchange("")   // default exchange routes by queue name
                .deadLetterRoutingKey(properties.ocrDeadLetterQueue())
                .build();
    }

    @Bean
    public Queue ocrDeadLetterQueue(MessagingProperties properties) {
        return QueueBuilder.durable(properties.ocrDeadLetterQueue()).build();
    }

    @Bean
    public Binding ocrQueueBinding(Queue ocrQueue, TopicExchange documentExchange, MessagingProperties properties) {
        return BindingBuilder.bind(ocrQueue).to(documentExchange).with(properties.documentUploadedRoutingKey());
    }

    // messages are sent as JSON, so the workers don't depend on Java classes of this application
    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }
}
