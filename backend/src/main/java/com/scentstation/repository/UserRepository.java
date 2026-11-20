package com.scentstation.repository;

import com.scentstation.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

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
}
