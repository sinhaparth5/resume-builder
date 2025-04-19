package com.example.resume_builder.model;

import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.*;

@Entity
public class Resume {
    @Id
    @Column(name = "id", length = 26, nullable = false, updatable = false)
    private String id;

    private String name;
    private String email;
    private String education;
    private String experience;
    private String skills;
    private String publications;
    private String awards;
    private String extraTitle;
    private String extraPoints;

    @Transient
    private static final ULID ulid = new ULID();

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = ulid.nextULID();
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getPublications() { return publications; }
    public void setPublications(String publications) { this.publications = publications; }
    public String getAwards() { return awards; }
    public void setAwards(String awards) { this.awards = awards; }
    public String getExtraTitle() { return extraTitle; }
    public void setExtraTitle(String extraTitle) { this.extraTitle = extraTitle; }
    public String getExtraPoints() { return extraPoints; }
    public void setExtraPoints(String extraPoints) { this.extraPoints = extraPoints; }
}
