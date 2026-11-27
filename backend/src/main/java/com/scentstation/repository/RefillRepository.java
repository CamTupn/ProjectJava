package com.scentstation.repository;

import com.scentstation.entity.Refill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefillRepository extends JpaRepository<Refill, Long> {

    @Override
    @EntityGraph(attributePaths = {"slot", "slot.machine", "product"})
    Optional<Refill> findById(Long id);

    @EntityGraph(attributePaths = {"slot", "slot.machine", "product"})
    List<Refill> findBySlot_IdOrderByRefilledAtDescIdDesc(Long slotId, Pageable pageable);
}
