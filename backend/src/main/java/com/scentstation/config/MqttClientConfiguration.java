package com.scentstation.config;

import com.scentstation.mqtt.MqttProperties;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/** Creates the Paho client and its connection options. Connection is started by MqttSubscriber
 * after its callback has been registered, so the initial connectComplete callback is not lost. */
@Configuration
@ConditionalOnProperty(prefix = "scentstation.mqtt", name = "enabled", havingValue = "true")
public class MqttClientConfiguration {

    @Bean(destroyMethod = "close")
    public MqttClient mqttClient(MqttProperties properties) throws MqttException {
        String clientId = properties.getClientId();
        if (clientId == null || clientId.isBlank()) {
            clientId = "scentstation-backend-" + UUID.randomUUID();
        }
        return new MqttClient(properties.getBrokerUrl(), clientId);
    }

    @Bean
    public MqttConnectOptions mqttConnectOptions(MqttProperties properties) {
        if (properties.getUsername() == null || properties.getUsername().isBlank()
                || properties.getPassword() == null || properties.getPassword().isBlank()) {
            throw new IllegalStateException(
                    "MQTT_USERNAME and MQTT_PASSWORD must be configured when MQTT is enabled");
        }
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(properties.getConnectionTimeoutSeconds());
        options.setKeepAliveInterval(properties.getKeepAliveSeconds());
        options.setUserName(properties.getUsername());
        options.setPassword(properties.getPassword().toCharArray());
        return options;
    }
}
