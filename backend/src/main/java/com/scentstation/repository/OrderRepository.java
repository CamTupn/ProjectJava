package com.scentstation.repository;

import com.scentstation.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    java.util.Optional<Order> findByIdAndTenantId(Long id, Long tenantId);
}
