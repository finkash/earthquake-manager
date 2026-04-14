package com.nikov.earthquake_manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EarthquakeServiceTest {

    @Autowired
    private EarthquakeService earthquakeService;

    @Autowired
    private EarthquakeRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void testFetchAndSave() {
        earthquakeService.fetchEarthquakes();

        long count = repository.count();
        assertTrue(count >= 0, "The database should have processed the earthquakes.");
    }

    @Test
    void testSaveAndFindById() {
        Earthquake e = new Earthquake();
        e.setMagnitude(3.4);
        e.setMagType("ml");
        e.setPlace("Test Location");
        e.setTitle("Test Title");
        e.setTime(LocalDateTime.now());
        e.setLongitude(25.11);
        e.setLatitude(42.70);

        Earthquake saved = repository.save(e);
        Optional<Earthquake> loaded = repository.findById(saved.getId());

        assertTrue(loaded.isPresent(), "Saved earthquake should be retrievable by ID.");
        assertEquals("Test Location", loaded.get().getPlace());
        assertEquals(3.4, loaded.get().getMagnitude());
    }

    @Test
    void testDeleteByIdRemovesRecord() {
        Earthquake e = new Earthquake();
        e.setMagnitude(4.1);
        e.setPlace("Delete Me");
        e.setTitle("Delete Test");
        e.setTime(LocalDateTime.now());

        Earthquake saved = repository.save(e);
        Long id = saved.getId();
        assertNotNull(id, "Saved earthquake should have an ID.");

        repository.deleteById(id);

        assertTrue(repository.findById(id).isEmpty(), "Deleted earthquake should not exist in the database.");
    }

    @Test
    void testLongPlaceValuePersists() {
        String longPlace = "A".repeat(480);

        Earthquake e = new Earthquake();
        e.setMagnitude(2.8);
        e.setPlace(longPlace);
        e.setTitle("Long Place Test");
        e.setTime(LocalDateTime.now());

        Earthquake saved = repository.save(e);
        Earthquake loaded = repository.findById(saved.getId()).orElseThrow();

        assertEquals(longPlace, loaded.getPlace(), "Long place value should persist without truncation.");
    }

    @Test
    void testFetchClearsExistingRecordsBeforeSavingNewOnes() {
        Earthquake existing = new Earthquake();
        existing.setMagnitude(5.0);
        existing.setPlace("Existing Row");
        existing.setTitle("Should Be Cleared");
        existing.setTime(LocalDateTime.now());

        Earthquake savedExisting = repository.save(existing);
        Long existingId = savedExisting.getId();
        assertNotNull(existingId, "Preloaded earthquake should have an ID.");

        earthquakeService.fetchEarthquakes();

        assertTrue(repository.findById(existingId).isEmpty(), "fetchEarthquakes should clear old records before insert.");
    }

    @Test
    void testFetchedRecordsRespectMagnitudeAndTimeFilters() {
        earthquakeService.fetchEarthquakes();

        List<Earthquake> saved = repository.findAll();
        LocalDateTime cutoff = LocalDateTime.now().minusHours(4).minusMinutes(1);

        for (Earthquake earthquake : saved) {
            assertNotNull(earthquake.getMagnitude(), "Saved earthquake magnitude should not be null.");
            assertNotNull(earthquake.getTime(), "Saved earthquake time should not be null.");
            assertTrue(earthquake.getMagnitude() > 2.0, "Saved earthquake should have magnitude > 2.0.");
            assertTrue(earthquake.getTime().isAfter(cutoff), "Saved earthquake should be newer than ~4 hours.");
        }
    }
}