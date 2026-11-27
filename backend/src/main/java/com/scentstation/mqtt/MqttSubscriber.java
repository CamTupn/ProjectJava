package com.scentstation.mqtt;

import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "scentstation.mqtt", name = "enabled", havingValue = "true")
public class MqttSubscriber implements MqttCallbackExtended {
    private static final Logger log = LoggerFactory.getLogger(MqttSubscriber.class);

    private final MqttClient client;
    private final MqttConnectOptions connectOptions;
    private final MqttProperties properties;
    private final MqttMessageRouter router;

    public MqttSubscriber(MqttClient client, MqttConnectOptions connectOptions,
                          MqttProperties properties, MqttMessageRouter router) {
        this.client = client;
        this.connectOptions = connectOptions;
        this.properties = properties;
        this.router = router;
    }

    @PostConstruct
    public void start() {
        client.setCallback(this);
        try {
            if (!client.isConnected()) {
                client.connect(connectOptions);
            }
        } catch (MqttException e) {
            log.error("Unable to connect to MQTT broker {}", properties.getBrokerUrl(), e);
            throw new IllegalStateException("MQTT is enabled but broker connection failed", e);
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        try {
            String prefix = properties.getTopicPrefix() + "/+/";
            client.subscribe(
                    new String[]{prefix + "status", prefix + "event"},
                    new int[]{properties.getQos(), properties.getQos()});
            log.info("MQTT {} to {}; subscribed to device status and events",
                    reconnect ? "reconnected" : "connected", serverURI);
        } catch (MqttException e) {
            log.error("Unable to subscribe to device topics", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost; automatic reconnect is enabled", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        router.route(topic, message.getPayload());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // No outbound delivery bookkeeping is required by this transport-only task.
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent ignored) {
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            log.warn("Error while disconnecting MQTT client", e);
        }
    }
}
