package com.nikov.earthquake_manager;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class EarthquakeService {

    private final EarthquakeRepository repository;

    public EarthquakeService(EarthquakeRepository repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fetchEarthquakes() {
        // USGS API URL for all earthquakes in the last 24 hours
        String url = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson";
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            // 1. Get the main JSON response
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("features");

            if (features != null) {
                for (Map<String, Object> feature : features) {
                    // 2. Extract the 'properties' (mag, place, title, etc.)
                    Map<String, Object> props = (Map<String, Object>) feature.get("properties");
                    
                    // 3. Extract the 'geometry' (coordinates: long, lat)
                    Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
                    List<Double> coords = (List<Double>) geometry.get("coordinates");

                    Earthquake e = new Earthquake();
                    
                    // Set Magnitude and Mag Type
                    if (props.get("mag") != null) {
                        e.setMagnitude(Double.parseDouble(props.get("mag").toString()));
                    }
                    e.setMagType(props.get("magType") != null ? props.get("magType").toString() : "unknown");
                    
                    // Set Place and Title
                    e.setPlace(props.get("place").toString());
                    e.setTitle(props.get("title").toString());

                    // Set Time (USGS gives time in Milliseconds, we convert it to LocalDateTime)
                    long epochTime = Long.parseLong(props.get("time").toString());
                    e.setTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneId.systemDefault()));

                    // Set Longitude and Latitude from geometry section
                    e.setLongitude(coords.get(0));
                    e.setLatitude(coords.get(1));

                    // 4. Save to your PostgreSQL database
                    repository.save(e); 
                }
                System.out.println("Saved " + features.size() + " earthquakes");
            }
        } catch (Exception e) {
            System.out.println("Error while fetching data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}