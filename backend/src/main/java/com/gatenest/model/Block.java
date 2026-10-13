package com.gatenest.model;

import java.time.Instant;

public class Block {
    private Long id;
    private String name;
    private String description;
    private int totalFloors;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    
    private int totalFlats;

    public Block() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTotalFloors() { return totalFloors; }
    public void setTotalFloors(int totalFloors) { this.totalFloors = totalFloors; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public int getTotalFlats() { return totalFlats; }
    public void setTotalFlats(int totalFlats) { this.totalFlats = totalFlats; }
}
