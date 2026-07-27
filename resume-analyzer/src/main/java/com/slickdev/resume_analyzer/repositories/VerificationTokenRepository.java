package com.slickdev.resume_analyzer.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.entities.VerificationToken;

import jakarta.transaction.Transactional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID>{
   
    @Transactional
    void deleteAllByUser(User user);
    List<VerificationToken> findAllByUser(User user);
}
