package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.Store;
import com.flashfood.flash_food.entity.StoreStatus;
import com.flashfood.flash_food.entity.StoreType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Store entity
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    Optional<Store> findByIdAndStatus(Long id, StoreStatus status);
    
    List<Store> findByStatus(StoreStatus status);
    
    List<Store> findByType(StoreType type);
    
    List<Store> findByOwnerEmail(String ownerEmail);
    
    /**
     * Find stores by name (for search functionality)
     */
    @Query("""
        SELECT s FROM Store s 
        WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        AND s.status = 'ACTIVE'
    """)
    List<Store> searchByName(@Param("keyword") String keyword);
    
    /**
     * Find active stores with coordinates (for Redis Geo indexing)
     */
    @Query("""
        SELECT s FROM Store s 
        WHERE s.status = 'ACTIVE' 
        AND s.latitude IS NOT NULL 
        AND s.longitude IS NOT NULL
    """)
    List<Store> findActiveStoresWithCoordinates();
}
