package com.gatenest;

import com.gatenest.service.ReportService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReportServiceTest {

    @Test
    public void testCsvFormulaInjectionSanitization() {
        ReportService reportService = new ReportService(null, null);

        // Test dangerous formula prefixes
        assertEquals("\"Safe Text\"", reportService.sanitizeCsvField("Safe Text"));
        assertEquals("\"'=1+1\"", reportService.sanitizeCsvField("=1+1"));
        assertEquals("\"'+2*3\"", reportService.sanitizeCsvField("+2*3"));
        assertEquals("\"'-500\"", reportService.sanitizeCsvField("-500"));
        assertEquals("\"'@SUM(A1:A10)\"", reportService.sanitizeCsvField("@SUM(A1:A10)"));
        assertEquals("\"'\tTAB\"", reportService.sanitizeCsvField("\tTAB"));
        assertEquals("\"'\rRETURN\"", reportService.sanitizeCsvField("\rRETURN"));
        assertEquals("\"\"", reportService.sanitizeCsvField(null));
        assertEquals("\"John \"\"The Boss\"\"\"", reportService.sanitizeCsvField("John \"The Boss\""));
    }
}
