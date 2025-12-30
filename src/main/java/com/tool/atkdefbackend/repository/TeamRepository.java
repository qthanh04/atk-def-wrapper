package com.tool.atkdefbackend.repository;

import com.tool.atkdefbackend.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Integer> {

    Optional<TeamEntity> findByUsername(String username);

    Optional<TeamEntity> findByName(String name);

    boolean existsByUsername(String username);

    boolean existsByName(String name);

    boolean existsByIpAddress(String ipAddress);

    java.util.List<TeamEntity> findByRole(String role);
}
