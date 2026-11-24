package com.scentstation.repository;

import com.scentstation.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p where (:tenantId is null or p.tenantId = :tenantId) " +
           "and (:status is null or p.status = :status) " +
           "and (:name is null or lower(p.name) like lower(concat('%', :name, '%'))) " +
           "order by p.id desc")
    List<Product> search(@Param("tenantId") Long tenantId,
                          @Param("status") String status,
                          @Param("name") String name);

    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndSku(Long tenantId, String sku);

    @Query(
        value = """
            SELECT COUNT(*)
            FROM machine_slots
            WHERE product_id = :productId
            """,
        nativeQuery = true
    )
    long countMachineSlotsByProductId(
        @Param("productId") Long productId
    );

    @Query(value = "SELECT EXISTS(SELECT 1 FROM orders WHERE product_id = :productId)", nativeQuery = true)
    boolean hasOrderHistory(@Param("productId") Long productId);
}
