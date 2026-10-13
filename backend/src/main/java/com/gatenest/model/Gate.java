package com.gatenest.model;

import java.time.Instant;

public class Gate {
    private Long id;
    private String name;
    private String code;
    private String description;
    private GateType gateType;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private int activeGuardsCount;

    public Gate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public GateType getGateType() { return gateType; }
    public void setGateType(GateType gateType) { this.gateType = gateType; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public int getActiveGuardsCount() { return activeGuardsCount; }
    public void setActiveGuardsCount(int activeGuardsCount) { this.activeGuardsCount = activeGuardsCount; }
}
