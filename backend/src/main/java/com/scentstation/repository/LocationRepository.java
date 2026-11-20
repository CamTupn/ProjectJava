package com.scentstation.repository;

import com.scentstation.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByIdAndTenantId(Long id, Long tenantId);
}
