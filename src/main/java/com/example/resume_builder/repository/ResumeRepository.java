package com.example.resume_builder.repository;

import com.example.resume_builder.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, String> {
}
