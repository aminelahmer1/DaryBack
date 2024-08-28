package com.example.dari_back.MFA;

import com.example.dari_back.entities.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class MfaVerificationResponse {
    private String email;

    private String jwt;
    private boolean mfaRequired;
    private boolean authValid;
    private boolean tokenValid;
    private String message;
    private Set<Role> roles; // Ajouter ce champ pour les rôles de l'utilisateur

}
