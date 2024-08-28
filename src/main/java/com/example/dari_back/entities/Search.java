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
@Table(name = "Search")
public class Search {
    @Id
    private int Id_search;
    private  String Description;
    private String Details;
    private String Search_Date;

    @ManyToOne
    @JoinColumn(name = "User_id")
    private User user;
}
