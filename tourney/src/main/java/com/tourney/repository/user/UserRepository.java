package com.tourney.repository.user;

import com.tourney.domain.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    boolean existsByName(String name);

    List<User> findByNameContainingIgnoreCaseOrderByNameAsc(String namePart, Pageable pageable);

}