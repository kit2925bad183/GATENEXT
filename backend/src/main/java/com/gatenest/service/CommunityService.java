package com.gatenest.service;

import com.gatenest.dto.BlockDto;
import com.gatenest.dto.CommunitySettingsDto;
import com.gatenest.dto.FlatDto;
import com.gatenest.dto.GateDto;
import com.gatenest.exception.BadRequestException;
import com.gatenest.exception.ConflictException;
import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.*;
import com.gatenest.repository.BlockRepository;
import com.gatenest.repository.CommunitySettingsRepository;
import com.gatenest.repository.FlatRepository;
import com.gatenest.repository.GateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    private final CommunitySettingsRepository settingsRepository;
    private final BlockRepository blockRepository;
    private final FlatRepository flatRepository;
    private final GateRepository gateRepository;
    private final AuditLogService auditLogService;
    private final org.springframework.core.env.Environment environment;

    public CommunityService(
            CommunitySettingsRepository settingsRepository,
            BlockRepository blockRepository,
            FlatRepository flatRepository,
            GateRepository gateRepository,
            AuditLogService auditLogService,
            org.springframework.core.env.Environment environment
    ) {
        this.settingsRepository = settingsRepository;
        this.blockRepository = blockRepository;
        this.flatRepository = flatRepository;
        this.gateRepository = gateRepository;
        this.auditLogService = auditLogService;
        this.environment = environment;
    }

    public CommunitySettings getSettings() {
        CommunitySettings settings = settingsRepository.getSettings()
                .orElseThrow(() -> new ResourceNotFoundException("Community settings not configured."));
        boolean isDemo = java.util.Arrays.asList(environment.getActiveProfiles()).contains("demo")
                || Boolean.TRUE.equals(environment.getProperty("gatenest.demo-mode", Boolean.class, false))
                || (settings.getCommunityName() != null && settings.getCommunityName().toLowerCase().contains("demo"));
        settings.setDemoMode(isDemo);
        return settings;
    }

    public CommunitySettings updateSettings(CommunitySettingsDto dto, User actor, String clientIp) {
        CommunitySettings current = getSettings();
        current.setCommunityName(dto.getCommunityName());
        current.setAddress(dto.getAddress());
        current.setCity(dto.getCity());
        current.setPostalCode(dto.getPostalCode());
        current.setDefaultTimezone(dto.getDefaultTimezone());
        current.setContactPhone(dto.getContactPhone());
        current.setContactEmail(dto.getContactEmail());
        current.setVisitorPassValidityHours(dto.getVisitorPassValidityHours());

        CommunitySettings updated = settingsRepository.updateSettings(current);
        auditLogService.logAction(actor, "COMMUNITY_SETTINGS_UPDATED", "COMMUNITY", "1", clientIp, null);
        return updated;
    }

    public List<Block> getAllBlocks() {
        return blockRepository.findAll();
    }

    public Block getBlockById(Long id) {
        return blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Block not found with ID: " + id));
    }

    @Transactional
    public Block createBlock(BlockDto dto, User actor, String clientIp) {
        blockRepository.findByName(dto.getName()).ifPresent(b -> {
            throw new ConflictException("A block with name '" + dto.getName() + "' already exists.");
        });

        Block block = new Block();
        block.setName(dto.getName());
        block.setDescription(dto.getDescription());
        block.setTotalFloors(dto.getTotalFloors());
        block.setActive(dto.isActive());
        block = blockRepository.save(block);

        auditLogService.logAction(actor, "BLOCK_CREATED", "BLOCK", String.valueOf(block.getId()), clientIp, Map.of("name", block.getName()));
        return block;
    }

    @Transactional
    public Block updateBlock(Long id, BlockDto dto, User actor, String clientIp) {
        Block existing = getBlockById(id);
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setTotalFloors(dto.getTotalFloors());
        existing.setActive(dto.isActive());
        Block updated = blockRepository.save(existing);

        auditLogService.logAction(actor, "BLOCK_UPDATED", "BLOCK", String.valueOf(id), clientIp, null);
        return updated;
    }

    public List<Flat> getAllFlats() {
        return flatRepository.findAll();
    }

    public List<Flat> getFlatsByBlockId(Long blockId) {
        return flatRepository.findByBlockId(blockId);
    }

    public Flat getFlatById(Long id) {
        return flatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flat not found with ID: " + id));
    }

    @Transactional
    public Flat createFlat(FlatDto dto, User actor, String clientIp) {
        Block block = getBlockById(dto.getBlockId());

        flatRepository.findByBlockIdAndFlatNumber(block.getId(), dto.getFlatNumber()).ifPresent(f -> {
            throw new ConflictException("Flat '" + dto.getFlatNumber() + "' already exists in " + block.getName());
        });

        Flat flat = new Flat();
        flat.setBlockId(block.getId());
        flat.setFlatNumber(dto.getFlatNumber());
        flat.setFloorNumber(dto.getFloorNumber());
        flat.setExtensionNumber(dto.getExtensionNumber());
        flat.setActive(dto.isActive());
        flat = flatRepository.save(flat);

        auditLogService.logAction(actor, "FLAT_CREATED", "FLAT", String.valueOf(flat.getId()), clientIp, Map.of(
                "blockName", block.getName(),
                "flatNumber", flat.getFlatNumber()
        ));
        return flat;
    }

    @Transactional
    public Flat updateFlat(Long id, FlatDto dto, User actor, String clientIp) {
        Flat existing = getFlatById(id);
        existing.setBlockId(dto.getBlockId());
        existing.setFlatNumber(dto.getFlatNumber());
        existing.setFloorNumber(dto.getFloorNumber());
        existing.setExtensionNumber(dto.getExtensionNumber());
        existing.setActive(dto.isActive());
        Flat updated = flatRepository.save(existing);

        auditLogService.logAction(actor, "FLAT_UPDATED", "FLAT", String.valueOf(id), clientIp, null);
        return updated;
    }

    public List<Gate> getAllGates() {
        return gateRepository.findAll();
    }

    public List<Gate> getActiveGates() {
        return gateRepository.findActiveGates();
    }

    public Gate getGateById(Long id) {
        return gateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found with ID: " + id));
    }

    @Transactional
    public Gate createGate(GateDto dto, User actor, String clientIp) {
        gateRepository.findByCode(dto.getCode()).ifPresent(g -> {
            throw new ConflictException("A gate with code '" + dto.getCode() + "' already exists.");
        });

        Gate gate = new Gate();
        gate.setName(dto.getName());
        gate.setCode(dto.getCode());
        gate.setDescription(dto.getDescription());
        gate.setGateType(dto.getGateType());
        gate.setActive(dto.isActive());
        gate = gateRepository.save(gate);

        auditLogService.logAction(actor, "GATE_CREATED", "GATE", String.valueOf(gate.getId()), clientIp, Map.of("name", gate.getName(), "code", gate.getCode()));
        return gate;
    }

    @Transactional
    public Gate updateGate(Long id, GateDto dto, User actor, String clientIp) {
        Gate existing = getGateById(id);
        existing.setName(dto.getName());
        existing.setCode(dto.getCode());
        existing.setDescription(dto.getDescription());
        existing.setGateType(dto.getGateType());
        existing.setActive(dto.isActive());
        Gate updated = gateRepository.save(existing);

        auditLogService.logAction(actor, "GATE_UPDATED", "GATE", String.valueOf(id), clientIp, null);
        return updated;
    }

    public Map<String, Object> getPublicStructure() {
        List<Block> blocks = blockRepository.findAll().stream().filter(Block::isActive).collect(Collectors.toList());
        List<Flat> flats = flatRepository.findAll().stream().filter(Flat::isActive).collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("blocks", blocks);
        map.put("flats", flats);
        return map;
    }
}
