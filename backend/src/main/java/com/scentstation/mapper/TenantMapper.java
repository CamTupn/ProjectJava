package com.scentstation.mapper;

import com.scentstation.dto.response.TenantResponse;
import com.scentstation.entity.Tenant;

import org.springframework.stereotype.Component;

@Component
public class TenantMapper {

    public TenantResponse toResponse(Tenant tenant) {
        if (tenant == null) {
            return null;
        }

        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getCode(),
                tenant.getStatus(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}
