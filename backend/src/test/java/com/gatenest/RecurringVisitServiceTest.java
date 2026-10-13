package com.gatenest;

import com.gatenest.model.RecurringVisitRule;
import com.gatenest.service.RecurringVisitService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class RecurringVisitServiceTest {

    @Test
    public void testIsRuleValidNow() {
        RecurringVisitService service = new RecurringVisitService(null, null);

        RecurringVisitRule rule = new RecurringVisitRule();
        rule.setActive(true);
        rule.setValidFromDate(LocalDate.now().minusDays(10));
        rule.setValidToDate(LocalDate.now().plusDays(10));
        rule.setAllowedWeekdays("MON,TUE,WED,THU,FRI,SAT,SUN");
        rule.setAllowedStartTime("00:00");
        rule.setAllowedEndTime("23:59");

        assertTrue(service.isRuleValidNow(rule));

        // Test inactive rule
        rule.setActive(false);
        assertFalse(service.isRuleValidNow(rule));

        // Test expired date
        rule.setActive(true);
        rule.setValidToDate(LocalDate.now().minusDays(1));
        assertFalse(service.isRuleValidNow(rule));

        // Test disallowed weekday
        rule.setValidToDate(LocalDate.now().plusDays(10));
        rule.setAllowedWeekdays("NON_MATCHING_DAY");
        assertFalse(service.isRuleValidNow(rule));
    }
}
