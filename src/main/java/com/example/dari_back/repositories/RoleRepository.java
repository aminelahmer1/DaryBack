package com.example.dari_back.repositories;


import com.example.dari_back.entities.Role;
import com.example.dari_back.entities.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    @Query("SELECT r.name FROM Role r")
    List<RoleType> findAllRoleTypes();

    @Query("SELECT r.name, COUNT(r) FROM Role r GROUP BY r.name")
    List<Object[]> countRoles();
}
