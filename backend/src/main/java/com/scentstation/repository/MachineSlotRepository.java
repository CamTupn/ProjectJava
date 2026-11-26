package com.scentstation.repository;

import com.scentstation.entity.MachineSlot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineSlotRepository extends JpaRepository<MachineSlot, Long> {

    @EntityGraph(attributePaths = {"machine", "product"})
    List<MachineSlot> findAllByMachine_IdOrderBySlotNumber(Long machineId);

    @Override
    @EntityGraph(attributePaths = {"machine", "product"})
    Optional<MachineSlot> findById(Long id);

    boolean existsByMachine_IdAndSlotNumber(
            Long machineId,
            Integer slotNumber
    );

    boolean existsByMachine_IdAndSlotNumberAndIdNot(
            Long machineId,
            Integer slotNumber,
            Long id
    );
}
