package com.scentstation.repository;

import com.scentstation.entity.DispenseExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DispenseExecutionRepository extends JpaRepository<DispenseExecution, Long> {
    Optional<DispenseExecution> findByCommand_Id(Long commandId);
}
