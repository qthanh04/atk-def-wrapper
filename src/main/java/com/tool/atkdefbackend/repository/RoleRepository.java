package com.tool.atkdefbackend.repository;


import com.tool.atkdefbackend.entity.security.ERole;
import com.tool.atkdefbackend.entity.security.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    @Query("Select r from RoleEntity r where r.name = ?1")
    Optional<RoleEntity> findByName(ERole eRole);
}
