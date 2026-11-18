package com.scentstation.repository;

import com.scentstation.entity.Machine;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineRepository extends JpaRepository<Machine, Long> {

    @EntityGraph(attributePaths = "tenant")
    Optional<Machine> findById(Long id);
}
