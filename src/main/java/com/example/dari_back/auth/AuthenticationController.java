package com.example.dari_back.auth;


import com.example.dari_back.MFA.MfaVerificationResponse;
import com.example.dari_back.config.JWTServiceImpl;
import com.example.dari_back.entities.Role;
import com.example.dari_back.entities.User;
import com.example.dari_back.services.EmailService;
import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
//@RequestMapping("/api/user")
@RequiredArgsConstructor

public class AuthenticationController {
    private EmailService emailService;
    private final AuthenticationService service;
    private final AuthenticationProvider authenticationProvider;
    private final JWTServiceImpl jwtServicee;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated @RequestBody User user) {
        // Vérifiez si l'e-mail est banni
        if (service.isEmailBanned(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to register. This email address is banned.");
        } else {
            // Continuez avec l'inscription normale
            // Register User // Generate QR code using the Secret KEY
            try {
                return ResponseEntity.ok(service.registerUser(user));
            } catch (QrGenerationException e) {
                return ResponseEntity.internalServerError().body("Something went wrong. Try again.");
            }
        }
    }



    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<?> login(@Validated @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        // Récupérer les rôles de l'utilisateur
        Set<Role> roles = user.getAuthFromBase();
        try {
            if (service.isEmailBanned(loginRequest.getEmail())) {
                // Renvoyer une réponse indiquant que l'utilisateur est banni
                return ResponseEntity.ok(MfaVerificationResponse.builder()
                        .email(loginRequest.getEmail())
                        .tokenValid(Boolean.FALSE)
                        .authValid(Boolean.FALSE)
                        .mfaRequired(Boolean.FALSE)
                        .message("You are banned. Contact the administrator for more information.")
                        .jwt("")
                        .roles(roles)
                        .build());
            } else {
                // Authentifier l'utilisateur


                // Construire la réponse en incluant le rôle de l'utilisateur
                return ResponseEntity.ok(MfaVerificationResponse.builder()
                        .email(user.getEmail()) // Utiliser l'e-mail de l'utilisateur authentifié
                        .tokenValid(Boolean.FALSE)
                        .authValid(Boolean.TRUE)
                        .mfaRequired(Boolean.TRUE)
                        .message("User Authenticated using username and Password")
                        .jwt("")
                        .roles(roles)
                        .build());
            }
        } catch (Exception e) {

            // Gérer les erreurs d'authentification
            return ResponseEntity.ok(MfaVerificationResponse.builder()
                    .email(loginRequest.getEmail())
                    .tokenValid(Boolean.FALSE)
                    .authValid(Boolean.FALSE)
                    .mfaRequired(Boolean.FALSE)
                    .message("Invalid Credentials. Please try again.")
                    .jwt("")
                    .roles(roles)
                    .build());
        }
    }}


