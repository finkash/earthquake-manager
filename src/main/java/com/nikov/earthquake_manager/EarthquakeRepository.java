package com.nikov.earthquake_manager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // This handles all the SQL (Save, Delete, Find) for you
public interface EarthquakeRepository extends JpaRepository<Earthquake, Long> {
    // You don't need to write code here yet! Spring does it automatically.
}