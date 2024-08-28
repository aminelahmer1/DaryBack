package com.example.dari_back.MFA;

import com.example.dari_back.entities.User;
import com.example.dari_back.token.Token;
import com.example.dari_back.token.TokenRepository;
import com.example.dari_back.token.TokenType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
public class EmailConfirmationService {

    private final TokenRepository tokenRepository;

    @Autowired
    public EmailConfirmationService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public Token createTokenForUser(User user) {
        Token token = new Token();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setTokenType(TokenType.EMAIL_CONFIRMATION);


        return tokenRepository.save(token);
    }
}
