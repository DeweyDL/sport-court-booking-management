package com.sportcourt.modules.sport_type.entity;

import java.time.LocalDateTime;

public class SportType {
    private String sportId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private boolean isDeleted;

    public SportType() {
    }

    public SportType(String sportId, String name, String description, LocalDateTime createdAt, boolean isDeleted) {
        this.sportId = sportId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
    }

    public String getSportId() {
        return sportId;
    }

    public void setSportId(String sportId) {
        this.sportId = sportId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
