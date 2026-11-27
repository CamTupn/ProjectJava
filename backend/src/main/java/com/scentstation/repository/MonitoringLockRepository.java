package com.scentstation.repository;

import com.scentstation.entity.Machine;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonitoringLockRepository extends JpaRepository<Machine, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Machine m where m.id = :id")
    Optional<Machine> findByIdForUpdate(@Param("id") Long id);

    @Query("select m from Machine m where m.connectionStatus = 'ONLINE' and m.lastPingAt < :cutoff")
    List<Machine> findOnlineMachinesTimedOut(@Param("cutoff") LocalDateTime cutoff);

    List<Machine> findByTenantId(Long tenantId);

    Optional<Machine> findByIdAndTenantId(Long id, Long tenantId);
}
