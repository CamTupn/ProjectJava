package com.scentstation.mapper;

import com.scentstation.dto.response.MachineResponse;
import com.scentstation.entity.Location;
import com.scentstation.entity.Machine;
import org.springframework.stereotype.Component;

@Component
public class MachineMapper {

    public MachineResponse toResponse(Machine machine) {
        if (machine == null) {
            return null;
        }

        Location loc = machine.getLocation();
        MachineResponse.LocationInfo locationInfo = (loc != null)
                ? new MachineResponse.LocationInfo(
                        loc.getId(),
                        loc.getName(),
                        loc.getAddress(),
                        loc.getStatus()
                )
                : null;

        return new MachineResponse(
                machine.getId(),
                machine.getTenantId(),
                loc != null ? loc.getId() : null,
                locationInfo,
                machine.getMachineCode(),
                machine.getSerialNumber(),
                machine.getDeviceIdentity(),
                machine.getConnectionStatus(),
                machine.getOperationalStatus(),
                machine.getFirmwareVersion(),
                machine.getLastPingAt(),
                machine.getCreatedAt(),
                machine.getUpdatedAt()
        );
    }
}
