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
    @SuppressWarnings("unchecked") // Removes 'Unchecked conversion' warnings for clean code
    public void fetchEarthquakes() {
        
        // [REQ #5]: Clear table before starting to avoid duplicates
        repository.deleteAll(); 

        String url = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson";
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("features");

            // Define a 'cutoff' time (e.g., last 2 hours)
            long twoHoursAgo = System.currentTimeMillis() - (120 * 60 * 1000);

            if (features != null) {
                int savedCount = 0;
                for (Map<String, Object> feature : features) {
                    Map<String, Object> props = (Map<String, Object>) feature.get("properties");
                    
                    // Extract magnitude and time for filtering
                    Double mag = props.get("mag") != null ? Double.parseDouble(props.get("mag").toString()) : 0.0;
                    long epochTime = Long.parseLong(props.get("time").toString());

                    // Combined Filtering Logic
                    if (mag > 2.0 && epochTime > twoHoursAgo) {
                        Earthquake e = new Earthquake();
                        
                        e.setMagnitude(mag);
                        e.setMagType(props.get("magType") != null ? props.get("magType").toString() : "unknown");
                        e.setPlace(props.get("place") != null ? props.get("place").toString() : "Unknown Location");
                        e.setTitle(props.get("title") != null ? props.get("title").toString() : "No Title");
                        e.setTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneId.systemDefault()));

                        Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
                        List<Double> coords = (List<Double>) geometry.get("coordinates");
                        e.setLongitude(coords.get(0));
                        e.setLatitude(coords.get(1));

                        repository.save(e); 
                        savedCount++;
                    }
                }
                System.out.println("Processed " + features.size() + " items and saved " + savedCount + " earthquakes.");
            }
        } catch (Exception e) {
            System.err.println("Error" + e.getMessage());
        }
    }
}