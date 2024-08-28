package com.example.dari_back.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Component
@ToString
@Getter
@Setter
@Table(name = "User")
public class Subscribe {
    @Id
    private int Id_subscribe;

    @ManyToOne
    @JoinColumn(name = "User_id")
    private User user;
}
