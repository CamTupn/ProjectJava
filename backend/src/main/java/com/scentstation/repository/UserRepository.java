package com.scentstation.repository;

import com.scentstation.entity.User;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"tenant", "roles"})
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"tenant", "roles"})
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Override
    @EntityGraph(attributePaths = {"tenant", "roles"})
    List<User> findAll();

    @Override
    @EntityGraph(attributePaths = {"tenant", "roles"})
    Optional<User> findById(Long id);

    @EntityGraph(attributePaths = {"tenant", "roles"})
    List<User> findAllByTenant_Id(Long tenantId);

    @EntityGraph(attributePaths = {"tenant", "roles"})
    Optional<User> findByIdAndTenant_Id(
            Long id,
            Long tenantId
    );

    // Lock user row when changing or resetting password
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(
            @Param("id") Long id
    );
}
