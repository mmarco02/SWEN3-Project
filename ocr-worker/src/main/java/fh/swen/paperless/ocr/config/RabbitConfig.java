package fh.swen.paperless.ocr.config;

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
 * Same topology as in the REST server: topic exchange, OCR queue bound with "document.uploaded"
 * and a dead letter queue for messages that could not be processed.
 * Declared on both sides, so it doesn't matter which application starts first.
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

    // the REST server puts its own class name into the type header; the worker ignores it
    // and converts the JSON into the parameter type of the listener method instead
    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(jsonMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
