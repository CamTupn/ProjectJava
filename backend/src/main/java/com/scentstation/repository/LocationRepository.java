package com.scentstation.repository;

import com.scentstation.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query("select l from Location l where (:tenantId is null or l.tenantId = :tenantId) " +
           "and (:status is null or l.status = :status) " +
           "and (:name is null or lower(l.name) like lower(concat('%', :name, '%'))) " +
           "order by l.id desc")
    List<Location> search(@Param("tenantId") Long tenantId,
                          @Param("status") String status,
                          @Param("name") String name);

    Optional<Location> findByIdAndTenantId(Long id, Long tenantId);

    @Query(
        value = """
            SELECT COUNT(*)
            FROM machines
            WHERE location_id = :locationId
            """,
        nativeQuery = true
    )
    long countMachinesByLocationId(@Param("locationId") Long locationId);
}
