package com.nikov.earthquake_manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import org.springframework.web.bind.annotation.*; 
// Changed to include @DeleteMapping and @PathVariable


@RestController
@RequestMapping("/api/earthquakes")
public class EarthquakeController {

    @Autowired
    private EarthquakeRepository repository;

    
    //Provides the data for the frontend table view
    @GetMapping
    public List<Earthquake> getAllEarthquakes() {
        return repository.findAll();
    }

    //Implementation for deleting a specific earthquake record 
    @DeleteMapping("/{id}")
    public void deleteEarthquake(@PathVariable Long id) {
        repository.deleteById(id);
    }
}