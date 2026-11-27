package com.scentstation.repository;

import com.scentstation.entity.DispenseCommand;
import com.scentstation.entity.DispenseCommandStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface DispenseCommandRepository extends JpaRepository<DispenseCommand, Long> {
    Optional<DispenseCommand> findByCommandToken(String commandToken);
    Optional<DispenseCommand> findByOrder_Id(Long orderId);
    @EntityGraph(attributePaths = {"order", "machine", "slot"})
    List<DispenseCommand> findByStatusIn(Collection<DispenseCommandStatus> statuses);
    @EntityGraph(attributePaths = {"order", "machine", "slot"})
    List<DispenseCommand> findByMachine_IdAndStatus(Long machineId, DispenseCommandStatus status);
    @EntityGraph(attributePaths = {"order", "machine", "slot"})
    List<DispenseCommand> findByMachine_Id(Long machineId);
    @EntityGraph(attributePaths = {"order", "machine", "slot"})
    List<DispenseCommand> findByStatus(DispenseCommandStatus status);
    @EntityGraph(attributePaths = {"order", "machine", "slot"})
    List<DispenseCommand> findAllByOrderByCreatedAtDesc();
}
