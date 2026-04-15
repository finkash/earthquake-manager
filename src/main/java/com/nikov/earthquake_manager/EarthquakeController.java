package com.nikov.earthquake_manager;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequestMapping("/api/earthquakes")
public class EarthquakeController {

    private final EarthquakeRepository repository;
    private final EarthquakeService earthquakeService;

    public EarthquakeController(EarthquakeRepository repository, EarthquakeService earthquakeService) {
        this.repository = repository;
        this.earthquakeService = earthquakeService;
    }

    
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

    @PostMapping("/refresh")
    public List<Earthquake> refreshEarthquakes() {
        earthquakeService.refreshEarthquakes();
        return repository.findAll();
    }
}