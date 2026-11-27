package com.scentstation.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "scentstation.mqtt")
public class MqttProperties {
    private boolean enabled = false;
    private String brokerUrl = "tcp://localhost:1883";
    private String clientId = "scentstation-backend";
    private String username = "";
    private String password = "";
    private int connectionTimeoutSeconds = 10;
    private int keepAliveSeconds = 30;
    private int qos = 1;
    private boolean commandRetained = false;
    private String topicPrefix = "scentstation";
    private Map<String, String> devices = new HashMap<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBrokerUrl() { return brokerUrl; }
    public void setBrokerUrl(String brokerUrl) { this.brokerUrl = brokerUrl; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getConnectionTimeoutSeconds() { return connectionTimeoutSeconds; }
    public void setConnectionTimeoutSeconds(int v) { this.connectionTimeoutSeconds = v; }
    public int getKeepAliveSeconds() { return keepAliveSeconds; }
    public void setKeepAliveSeconds(int v) { this.keepAliveSeconds = v; }
    public int getQos() { return qos; }
    public void setQos(int qos) { this.qos = qos; }
    public boolean isCommandRetained() { return commandRetained; }
    public void setCommandRetained(boolean commandRetained) { this.commandRetained = commandRetained; }
    public String getTopicPrefix() { return topicPrefix; }
    public void setTopicPrefix(String topicPrefix) { this.topicPrefix = topicPrefix; }
    public Map<String, String> getDevices() { return devices; }
    public void setDevices(Map<String, String> devices) { this.devices = devices; }
}
