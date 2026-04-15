package com.nikov.earthquake_manager;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity //Make a database table for this class
@Table(name = "earthquakes")
@Data   //Lombok - creates Getters/Setters automatically
public class Earthquake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double magnitude;
    private String magType;
    
    @Column(length = 500)
    private String place;

    private String title;
    private LocalDateTime time;

    //Should be Double instead of double to allow for null values in case of missing data
    private Double longitude;
    private Double latitude;
}