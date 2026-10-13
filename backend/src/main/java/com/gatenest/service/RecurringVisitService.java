package com.gatenest.service;

import com.gatenest.exception.BadRequestException;
import com.gatenest.exception.ResourceNotFoundException;
import com.gatenest.model.RecurringVisitRule;
import com.gatenest.model.ResidentProfile;
import com.gatenest.repository.RecurringVisitRuleRepository;
import com.gatenest.repository.ResidentProfileRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RecurringVisitService {

    private final RecurringVisitRuleRepository ruleRepository;
    private final ResidentProfileRepository residentProfileRepository;

    public RecurringVisitService(RecurringVisitRuleRepository ruleRepository,
                                 ResidentProfileRepository residentProfileRepository) {
        this.ruleRepository = ruleRepository;
        this.residentProfileRepository = residentProfileRepository;
    }

    public List<RecurringVisitRule> getRulesForResident(Long residentUserId) {
        return ruleRepository.findByResidentUserId(residentUserId);
    }

    public List<RecurringVisitRule> getAllActiveRules() {
        return ruleRepository.findAllActive();
    }

    public RecurringVisitRule createRule(Long residentUserId, RecurringVisitRule rule) {
        ResidentProfile profile = residentProfileRepository.findByUserId(residentUserId)
                .orElseThrow(() -> new BadRequestException("Resident profile not found for user: " + residentUserId));

        if (rule.getValidFromDate() == null || rule.getValidToDate() == null) {
            throw new BadRequestException("Valid from and to dates are required");
        }
        if (rule.getValidToDate().isBefore(rule.getValidFromDate())) {
            throw new BadRequestException("Valid to date cannot be before valid from date");
        }

        rule.setId(null);
        rule.setResidentUserId(residentUserId);
        rule.setDestinationFlatId(profile.getFlatId());
        rule.setActive(true);
        return ruleRepository.save(rule);
    }

    public RecurringVisitRule toggleRuleStatus(Long residentUserId, Long ruleId, boolean active) {
        RecurringVisitRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring rule not found with id: " + ruleId));
        if (!rule.getResidentUserId().equals(residentUserId)) {
            throw new BadRequestException("Not authorized to modify this recurring rule");
        }
        rule.setActive(active);
        ruleRepository.updateStatus(ruleId, active);
        return rule;
    }

    public boolean isRuleValidNow(RecurringVisitRule rule) {
        if (!rule.isActive()) return false;
        LocalDate today = LocalDate.now();
        if (today.isBefore(rule.getValidFromDate()) || today.isAfter(rule.getValidToDate())) {
            return false;
        }
        DayOfWeek dow = today.getDayOfWeek();
        String dayCode = dow.name().substring(0, 3).toUpperCase();
        if (rule.getAllowedWeekdays() != null && !rule.getAllowedWeekdays().toUpperCase().contains(dayCode)) {
            return false;
        }

        try {
            LocalTime nowTime = LocalTime.now();
            LocalTime start = LocalTime.parse(rule.getAllowedStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime end = LocalTime.parse(rule.getAllowedEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
            if (nowTime.isBefore(start) || nowTime.isAfter(end)) {
                return false;
            }
        } catch (Exception ignored) {
            // Default allow if parsing fails
        }

        return true;
    }
}
