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
        refreshEarthquakes();
    }

    @SuppressWarnings("unchecked")
    public int refreshEarthquakes() {

        //Clear table before starting to avoid duplicates
        repository.deleteAll(); 

        String url = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson";
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            //API Unavailability check
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            // Null check for the main response body
            if (response == null || !response.containsKey("features")) {
                System.out.println("API returned no features.");
                return 0;
            }

            List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("features");

            //Define a 'cutoff' time - 4 hours ago
            long fourHoursAgo = System.currentTimeMillis() - (240 * 60 * 1000);

            if (features != null) {
                int savedCount = 0;
                for (Map<String, Object> feature : features) {
                    
                    //Safety check for properties section and geometry section
                    Map<String, Object> props = (Map<String, Object>) feature.get("properties");
                    Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
                    
                    if (props == null || geometry == null) continue; 

                    //Null check for mag. If fine, extract and parse it. If not, default to 0.0
                    Double mag = props.get("mag") != null ? Double.parseDouble(props.get("mag").toString()) : 0.0;
                    
                    //Safety check for time
                    if (props.get("time") == null) continue;
                    long epochTime = Long.parseLong(props.get("time").toString());

                    //Filtering - magnitude and time
                    if (mag > 2.0 && epochTime > fourHoursAgo) {
                        Earthquake e = new Earthquake();
                        
                        e.setMagnitude(mag);
                        
                        e.setMagType(props.get("magType") != null ? props.get("magType").toString() : "unknown");
                        e.setPlace(props.get("place") != null ? props.get("place").toString() : "Unknown Location");
                        e.setTitle(props.get("title") != null ? props.get("title").toString() : "No Title");
                        
                        e.setTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneId.systemDefault()));

                        //Safety check for coordinates
                        List<Object> coords = (List<Object>) geometry.get("coordinates");
                        if (coords != null && coords.size() >= 2) {
                            e.setLongitude(Double.parseDouble(coords.get(0).toString()));
                            e.setLatitude(Double.parseDouble(coords.get(1).toString()));
                        }

                        //Database error check - jumps to catch if save fails
                        repository.save(e); 
                        savedCount++;
                    }
                }
                System.out.println("Processed " + features.size() + " items and saved " + savedCount + " earthquakes.");
                return savedCount;
            }
        } catch (Exception e) {
            //Catches API issues, database errors, or data parsing problems
            System.err.println("ERROR: " + e.getMessage());
        }

        return 0;
    }
}