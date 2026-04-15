package com.nikov.earthquake_manager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository: This annotation is used to indicate that the class provides the mechanism for storage, retrieval, search, update and delete operation on objects.
// JpaRepository: This is a JPA specific extension of Repository. It contains the API for basic CRUD operations and also API for pagination and sorting.  
@Repository 
public interface EarthquakeRepository extends JpaRepository<Earthquake, Long> {
    
}