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
@Table(name = "Announcement")
public class Announcement {
    @Id
    private  int Id_announcement;

    private String title;
    private String description;
    private String Date;

    @Enumerated(EnumType.STRING)
    private Type_Announcement typeAnnouncement;

    @ManyToOne
    @JoinColumn(name = "Id_realestate")
    private Real_Estate realEstate ;
    @ManyToOne(optional = false)
    private Real_Estate realEstates;

    public Real_Estate getRealEstates() {
        return realEstates;
    }

    public void setRealEstates(Real_Estate realEstates) {
        this.realEstates = realEstates;
    }
}
