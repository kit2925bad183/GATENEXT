package com.gatenest.dto;

import com.gatenest.model.GateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class BlockDto {
    private Long id;

    @NotBlank(message = "Block name is required")
    private String name;

    private String description;

    @Positive(message = "Total floors must be at least 1")
    private int totalFloors = 1;

    private boolean active = true;
    private int totalFlats;

    public BlockDto() {}

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

    public int getTotalFlats() { return totalFlats; }
    public void setTotalFlats(int totalFlats) { this.totalFlats = totalFlats; }
}
