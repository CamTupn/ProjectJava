package com.scentstation.mapper;

import com.scentstation.dto.response.ProductResponse;
import com.scentstation.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getTenantId(), p.getSku(), p.getName(),
                p.getDescription(), p.getFragranceNotes(), p.getImageUrl(), p.getPrice(),
                p.getVolumePerSprayMl(), p.getStatus(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
