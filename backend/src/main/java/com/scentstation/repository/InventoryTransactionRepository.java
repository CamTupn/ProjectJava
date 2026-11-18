package com.scentstation.repository;

import com.scentstation.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long> {
}
