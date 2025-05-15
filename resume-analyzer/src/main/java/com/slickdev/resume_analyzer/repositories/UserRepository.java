package com.slickdev.resume_analyzer.repositories;


import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.slickdev.resume_analyzer.entities.User;


public interface UserRepository extends JpaRepository<User, UUID> {
    
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    Optional<User> findByUserNameOrEmail (String userName, String email);
    
}
