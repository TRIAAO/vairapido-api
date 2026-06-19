package com.vairapido.api.controller;

import com.vairapido.api.dto.report.OperationalReportResponse;
import com.vairapido.api.service.OperationalReportService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports/operational")
public class OperationalReportController {

    private final OperationalReportService operationalReportService;

    public OperationalReportController(
            OperationalReportService operationalReportService
    ) {
        this.operationalReportService = operationalReportService;
    }

    @GetMapping
    public OperationalReportResponse getGlobalReport() {
        return operationalReportService.getGlobalReport();
    }

    @GetMapping("/company/{companyId}")
    public OperationalReportResponse getCompanyReport(
            @PathVariable UUID companyId
    ) {
        return operationalReportService.getCompanyReport(companyId);
    }
}