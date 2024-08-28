package com.example.dari_back.token;


import com.example.dari_back.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Integer id;

    @Column(unique = true)
    public String token;

    @Column(length = 20) // Assurez-vous que la longueur est appropriée
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;


    public boolean revoked;

    public boolean expired;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;

    public String getToken() {
        return this.token;
    }
}
