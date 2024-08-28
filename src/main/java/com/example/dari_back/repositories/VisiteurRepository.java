package com.example.dari_back.repositories;

import com.example.dari_back.entities.Visiteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VisiteurRepository extends JpaRepository<Visiteur,Integer> {
    Optional<?> findVisiteurByUser(Integer user);
}
