package com.scentstation.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MqttSubscriberTest {
    @Test
    void registersCallbackAndConnectsBeforeSubscribing() throws Exception {
        MqttClient client = mock(MqttClient.class);
        when(client.isConnected()).thenReturn(false, true);
        MqttProperties properties = new MqttProperties();
        MqttSubscriber subscriber = new MqttSubscriber(
                client, new MqttConnectOptions(), properties, mock(MqttMessageRouter.class));

        subscriber.start();

        verify(client).setCallback(subscriber);
        verify(client).connect(any(MqttConnectOptions.class));
        // Paho invokes connectComplete after successful initial connection.
        subscriber.connectComplete(false, "tcp://localhost:1883");
        verify(client).subscribe(
                org.mockito.ArgumentMatchers.<String[]>argThat(topics -> topics.length == 2
                        && "scentstation/+/status".equals(topics[0])
                        && "scentstation/+/event".equals(topics[1])),
                org.mockito.ArgumentMatchers.<int[]>argThat(qos ->
                        qos.length == 2 && qos[0] == 1 && qos[1] == 1));
    }

    @Test
    void subscribesAgainAfterReconnect() throws Exception {
        MqttClient client = mock(MqttClient.class);
        when(client.isConnected()).thenReturn(true);
        MqttProperties properties = new MqttProperties();
        MqttSubscriber subscriber = new MqttSubscriber(
                client, new MqttConnectOptions(), properties, mock(MqttMessageRouter.class));

        subscriber.start();
        subscriber.connectComplete(true, "tcp://localhost:1883");

        verify(client).setCallback(subscriber);
        verify(client, times(1)).subscribe(
                any(String[].class), org.mockito.ArgumentMatchers.<int[]>any());
    }
}
