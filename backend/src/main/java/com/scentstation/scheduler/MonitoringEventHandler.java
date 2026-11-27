package com.scentstation.scheduler;

import com.scentstation.contract.*;
import com.scentstation.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonitoringEventHandler {

    private final MonitoringService monitoringService;

    @EventListener
    public void onHeartbeat(Heartbeat event) {
        try {
            monitoringService.processHeartbeat(event);
        } catch (Exception e) {
            log.error("Lỗi xử lý Heartbeat event", e);
        }
    }

    @EventListener
    public void onTelemetry(Telemetry event) {
        try {
            monitoringService.processTelemetry(event);
        } catch (Exception e) {
            log.error("Lỗi xử lý Telemetry event", e);
        }
    }

    @EventListener
    public void onDeviceFault(DeviceFault event) {
        try {
            monitoringService.processDeviceFault(event);
        } catch (Exception e) {
            log.error("Lỗi xử lý DeviceFault event", e);
        }
    }

    @EventListener
    public void onDeviceAck(DeviceAck event) {
        try {
            monitoringService.processDeviceAck(event);
        } catch (Exception e) {
            log.error("Lỗi xử lý DeviceAck event", e);
        }
    }

    @EventListener
    public void onDeviceResult(DeviceResult event) {
        try {
            monitoringService.processDeviceResult(event);
        } catch (Exception e) {
            log.error("Lỗi xử lý DeviceResult event", e);
        }
    }
}