package com.scentstation.mapper;

import com.scentstation.dto.response.LocationResponse;
import com.scentstation.entity.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {
    public LocationResponse toResponse(Location l) {
        return new LocationResponse(l.getId(), l.getTenantId(), l.getName(), l.getAddress(),
                l.getDescription(), l.getStatus(), l.getCreatedAt(), l.getUpdatedAt());
    }
}
