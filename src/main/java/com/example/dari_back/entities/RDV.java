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
@Table(name = "RDV")
public class RDV {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Id_RDV;
    private String Date;
    private String Time;

    @ManyToOne
    @JoinColumn(name = "User_id")
    private User user;
}
