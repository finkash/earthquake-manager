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
    private String place;
    private String title;
    private LocalDateTime time;
    private Double longitude;
    private Double latitude;
}