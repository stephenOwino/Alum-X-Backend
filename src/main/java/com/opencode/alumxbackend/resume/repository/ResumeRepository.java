package com.opencode.alumxbackend.resume.repository;

import com.opencode.alumxbackend.resume.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume,String> {
    Optional<Resume> findByUserId(String userId);
    void deleteByUserId(String userId);
}
