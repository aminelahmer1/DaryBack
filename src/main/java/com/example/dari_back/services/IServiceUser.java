package com.example.dari_back.services;

import com.example.dari_back.MFA.MfaTokenData;
import com.example.dari_back.entities.User;
import com.example.dari_back.exception.InvalidTokenException;
import com.example.dari_back.exception.UserAlreadyExistException;
import dev.samstevens.totp.exceptions.QrGenerationException;

public interface IServiceUser {
    MfaTokenData registerUser(User user) throws UserAlreadyExistException, QrGenerationException;

    boolean verifyUser(final String token) throws InvalidTokenException;
    public boolean verifyTotp(String code, String username);
}
