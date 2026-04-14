package com.nikov.earthquake_manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/earthquakes") // This is the URL the frontend will call
public class EarthquakeController {

    @Autowired
    private EarthquakeRepository repository;

    @GetMapping
    public List<Earthquake> getAllEarthquakes() {
        // This returns only the filtered earthquakes currently in the DB
        return repository.findAll();
    }
}