package com.scentstation.contract;

public interface DispensePort {
    CommandView createCommand(DispenseRequest request);
}
