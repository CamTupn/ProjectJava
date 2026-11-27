package com.scentstation.mapper;

import com.scentstation.dto.response.DispenseCommandResponse;
import com.scentstation.dto.response.DispenseExecutionResponse;
import com.scentstation.entity.DispenseCommand;
import com.scentstation.entity.DispenseExecution;
import org.springframework.stereotype.Component;

@Component
public class DispenseMapper {
    public DispenseCommandResponse toCommandResponse(DispenseCommand command) {
        return DispenseCommandResponse.from(command);
    }
    public DispenseExecutionResponse toExecutionResponse(DispenseExecution execution) {
        return DispenseExecutionResponse.from(execution);
    }
}
