package com.scentstation.contract;

import com.scentstation.entity.DispenseCommand;

public interface DispensePublisher {
    void publish(DispenseCommand command);
}
