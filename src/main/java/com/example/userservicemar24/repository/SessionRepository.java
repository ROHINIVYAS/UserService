package com.example.userservicemar24.repository;

import com.example.userservicemar24.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByTokenAndUser_id(String token, Long userId);
}
