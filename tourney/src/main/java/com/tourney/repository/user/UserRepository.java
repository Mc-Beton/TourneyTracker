package com.tourney.repository.user;

import com.common.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);
    boolean existsByName(String name);

    List<User> findByNameContainingIgnoreCaseOrderByNameAsc(String namePart, Pageable pageable);

    @Query("SELECT DISTINCT u.city FROM User u WHERE LOWER(u.city) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY u.city ASC")
    List<String> findDistinctCities(@Param("query") String query, Pageable pageable);
}