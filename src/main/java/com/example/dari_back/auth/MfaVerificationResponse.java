package com.example.dari_back.auth;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MfaVerificationResponse {

    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String role;
}
