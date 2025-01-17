package com.example.dari_back.repositories;

import com.example.dari_back.entities.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface SellerRepository extends JpaRepository<Seller, Integer> {
    Optional<?> findSellerByUser(Integer user);
}