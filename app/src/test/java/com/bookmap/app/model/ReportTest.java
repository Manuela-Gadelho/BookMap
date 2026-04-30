package com.bookmap.app.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes unitarios para o modelo Report.
 * Verifica getters/setters e valores de status.
 */
public class ReportTest {

    @Test
    public void testDefaultConstructor() {
        Report report = new Report();
        assertEquals(0, report.getId());
        assertNull(report.getReason());
    }

    @Test
    public void testSettersAndGetters() {
        Report report = new Report();
        report.setId(1);
        report.setReporterId(10);
        report.setReportedUserId(20);
        report.setReportedContentId(30);
        report.setContentType("REVIEW");
        report.setReason("Conteudo inapropriado");
        report.setStatus("PENDING");
        report.setCreatedAt("2025-01-01");

        assertEquals(1, report.getId());
        assertEquals(10, report.getReporterId());
        assertEquals(20, report.getReportedUserId());
        assertEquals(30, report.getReportedContentId());
        assertEquals("REVIEW", report.getContentType());
        assertEquals("Conteudo inapropriado", report.getReason());
        assertEquals("PENDING", report.getStatus());
        assertEquals("2025-01-01", report.getCreatedAt());
    }

    @Test
    public void testStatusValues() {
        Report report = new Report();

        report.setStatus("PENDING");
        assertEquals("PENDING", report.getStatus());

        report.setStatus("REVIEWED");
        assertEquals("REVIEWED", report.getStatus());

        report.setStatus("RESOLVED");
        assertEquals("RESOLVED", report.getStatus());
    }
}
