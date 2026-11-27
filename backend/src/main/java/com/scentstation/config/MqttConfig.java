package com.scentstation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scentstation.mqtt.MqttProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfig {

    /** MQTT wire DTOs use Jackson 2 (com.fasterxml.jackson) explicitly. */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper mqttObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
