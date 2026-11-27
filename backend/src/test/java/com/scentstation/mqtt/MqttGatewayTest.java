package com.scentstation.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scentstation.contract.DeviceCommand;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MqttGatewayTest {
    @Test void publishesExpectedTopicJsonQosAndRetain() throws Exception {
        MqttClient client = mock(MqttClient.class);
        MqttProperties properties = new MqttProperties();
        properties.setQos(1);
        properties.setCommandRetained(false);
        MqttGateway gateway = new MqttGateway(client, properties, new ObjectMapper().findAndRegisterModules());
        gateway.publishCommand(new DeviceCommand("cmd-1", "machine-1", "DISPENSE", "slot-1", "order-1", null, null));
        verify(client).publish(eq("scentstation/machine-1/command"), argThat(message -> {
            try {
                var json = new ObjectMapper().findAndRegisterModules().readTree(message.getPayload());
                return message.getQos() == 1 && !message.isRetained()
                        && "cmd-1".equals(json.get("commandId").asText())
                        && "machine-1".equals(json.get("machineId").asText());
            } catch (Exception e) { return false; }
        }));
    }

    @Test void rejectsCommandWithoutMachineId() {
        MqttGateway gateway = new MqttGateway(mock(MqttClient.class), new MqttProperties(), new ObjectMapper().findAndRegisterModules());
        assertThrows(IllegalArgumentException.class,
                () -> gateway.publishCommand(new DeviceCommand("cmd-1", " ", "DISPENSE", null, null, null, null)));
    }
}
