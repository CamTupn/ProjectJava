package com.scentstation.service;

import com.scentstation.contract.DispenseNotice;
import com.scentstation.entity.DispenseExecution;
import org.springframework.stereotype.Component;

@Component
public class DispenseEventHandler {
    private final DispenseService dispenseService;

    public DispenseEventHandler(DispenseService dispenseService) {
        this.dispenseService = dispenseService;
    }

    public void handleAck(String commandToken) {
        dispenseService.acknowledge(commandToken);
    }

    public DispenseExecution handleResult(DispenseNotice notice) {
        return dispenseService.recordNotice(notice);
    }
}
