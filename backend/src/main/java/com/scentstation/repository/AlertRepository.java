package com.scentstation.repository;

import com.scentstation.entity.Alert;
import com.scentstation.entity.AlertStatus;
import com.scentstation.entity.AlertType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    boolean existsByMachineIdAndTypeAndSlotIdAndStatusIn(
            Long machineId,
            AlertType type,
            Long slotId,
            Collection<AlertStatus> statuses
    );

    List<Alert> findByMachineIdAndTypeAndStatusIn(
            Long machineId,
            AlertType type,
            Collection<AlertStatus> statuses
    );

    Optional<Alert> findByIdAndTenantId(Long id, Long tenantId);

    Page<Alert> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Page<Alert> findByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, AlertStatus status, Pageable pageable);

    Page<Alert> findByTenantIdAndMachineIdOrderByCreatedAtDesc(Long tenantId, Long machineId, Pageable pageable);

    long countByTenantIdAndStatusIn(Long tenantId, Collection<AlertStatus> statuses);
}