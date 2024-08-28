package com.example.dari_back.MFA;

import lombok.Data;

@Data
public class MfaVerificationRequest {
    private String email;
    private String totp;
}
