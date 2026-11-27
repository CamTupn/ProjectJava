package com.scentstation.repository;

import com.scentstation.entity.DeviceEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceEventRepository extends JpaRepository<DeviceEvent, Long> {

    Page<DeviceEvent> findByTenantIdAndMachineIdOrderByOccurredAtDesc(
            Long tenantId,
            Long machineId,
            Pageable pageable
    );
}