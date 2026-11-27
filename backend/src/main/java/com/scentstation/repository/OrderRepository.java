package com.scentstation.repository;

import com.scentstation.entity.Order;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT o FROM Order o
            WHERE (:tenantId IS NULL OR o.tenantId = :tenantId)
              AND (:machineId IS NULL OR o.machineId = :machineId)
              AND (:status IS NULL OR o.status = :status)
            ORDER BY o.id DESC
            """)
    List<Order> search(
            @Param("tenantId") Long tenantId,
            @Param("machineId") Long machineId,
            @Param("status") String status
    );

    List<Order> findByStatusAndExpiresAtBefore(String status, LocalDateTime now);
}
