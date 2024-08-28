package com.example.dari_back.config;

import com.example.dari_back.entities.Role;
import org.springframework.security.core.Authentication;

import java.text.ParseException;
import java.util.Set;

public interface JWTServiceImpl {
    String generateJwt(String email, Set<Role> roles) throws ParseException;
    Authentication validateJwt(String jwt);
}
