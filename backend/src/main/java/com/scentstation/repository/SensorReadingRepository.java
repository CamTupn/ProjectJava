package com.scentstation.repository;

import com.scentstation.entity.SensorReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    Page<SensorReading> findByTenantIdAndMachineIdOrderByRecordedAtDesc(
            Long tenantId,
            Long machineId,
            Pageable pageable
    );
}