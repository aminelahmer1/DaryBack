package com.example.dari_back.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Component
@ToString
@Getter
@Setter
@Table(name = "Real_Estate")
public class Real_Estate {
    @Id
    private  int Id_realestate;

    private  String startLocation_longitude ;
    private  String startLocation_latitude ;
    private String endLocation_longitude;
    private String endLocation_latitude;

    @OneToMany
    @JsonIgnore
    private List<Announcement> announcements;
}
