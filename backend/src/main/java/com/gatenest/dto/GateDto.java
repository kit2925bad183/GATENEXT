package com.gatenest.dto;

import com.gatenest.model.GateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GateDto {
    private Long id;

    @NotBlank(message = "Gate name is required")
    private String name;

    @NotBlank(message = "Gate code is required")
    private String code;

    private String description;

    @NotNull(message = "Gate type is required")
    private GateType gateType = GateType.GENERAL;

    private boolean active = true;
    private int activeGuardsCount;

    public GateDto() {}

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

    public int getActiveGuardsCount() { return activeGuardsCount; }
    public void setActiveGuardsCount(int activeGuardsCount) { this.activeGuardsCount = activeGuardsCount; }
}
