package com.xiangyun.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.event.EventBusNames;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class OperationRabbitConfig {
    @Bean
    DirectExchange businessExchange() {
        return new DirectExchange(EventBusNames.EXCHANGE, true, false);
    }

    @Bean
    DirectExchange businessDeadExchange() {
        return new DirectExchange(EventBusNames.DEAD_EXCHANGE, true, false);
    }

    @Bean
    Queue workflowQueue() {
        return QueueBuilder.durable(EventBusNames.WORKFLOW_QUEUE)
                .deadLetterExchange(EventBusNames.DEAD_EXCHANGE)
                .deadLetterRoutingKey(EventBusNames.WORKFLOW_ROUTING_KEY)
                .build();
    }

    @Bean
    Queue workflowDeadQueue() {
        return QueueBuilder.durable(EventBusNames.WORKFLOW_DEAD_QUEUE).build();
    }

    @Bean
    Binding workflowBinding(Queue workflowQueue, DirectExchange businessExchange) {
        return BindingBuilder.bind(workflowQueue).to(businessExchange).with(EventBusNames.WORKFLOW_ROUTING_KEY);
    }

    @Bean
    Binding workflowDeadBinding(Queue workflowDeadQueue, DirectExchange businessDeadExchange) {
        return BindingBuilder.bind(workflowDeadQueue).to(businessDeadExchange).with(EventBusNames.WORKFLOW_ROUTING_KEY);
    }

    @Bean
    Jackson2JsonMessageConverter rabbitJsonConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.xiangyun.common.event");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
