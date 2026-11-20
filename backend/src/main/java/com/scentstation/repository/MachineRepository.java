package com.scentstation.repository;

import com.scentstation.entity.Machine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MachineRepository extends JpaRepository<Machine, Long> {

    @EntityGraph(attributePaths = {"location"})
    @Query("""
            SELECT m FROM Machine m
            WHERE (:tenantId IS NULL OR m.tenantId = :tenantId)
              AND (:connectionStatus IS NULL OR m.connectionStatus = :connectionStatus)
              AND (:operationalStatus IS NULL OR m.operationalStatus = :operationalStatus)
              AND (:locationId IS NULL OR m.location.id = :locationId)
            ORDER BY m.id DESC
            """)
    List<Machine> search(
            @Param("tenantId") Long tenantId,
            @Param("connectionStatus") String connectionStatus,
            @Param("operationalStatus") String operationalStatus,
            @Param("locationId") Long locationId
    );

    @EntityGraph(attributePaths = {"location"})
    Optional<Machine> findWithLocationById(Long id);

    @EntityGraph(attributePaths = {"location"})
    Optional<Machine> findWithLocationByIdAndTenantId(Long id, Long tenantId);

    boolean existsByMachineCode(String machineCode);

    boolean existsByMachineCodeAndIdNot(String machineCode, Long id);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    boolean existsByDeviceIdentity(String deviceIdentity);

    boolean existsByDeviceIdentityAndIdNot(String deviceIdentity, Long id);
}
