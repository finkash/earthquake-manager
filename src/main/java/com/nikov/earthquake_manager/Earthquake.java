package com.nikov.earthquake_manager;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity // This tells Spring: "Make a database table for this class"
@Table(name = "earthquakes")
@Data   // This is Lombok: It creates Getters/Setters automatically
public class Earthquake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double magnitude;
    private String magType;

    // Some place names can be very long
    @Column(length = 500)
    private String place;

    private String title;
    private LocalDateTime time;

    //Should be Double instead of double to allow for null values in case of missing data
    private Double longitude;
    private Double latitude;
}