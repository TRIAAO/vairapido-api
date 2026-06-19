package com.vairapido.api.controller;

import com.vairapido.api.dto.report.OperationalReportResponse;
import com.vairapido.api.service.OperationalReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public OperationalReportResponse getGlobalReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt
    ) {
        return operationalReportService.getGlobalReport(startAt, endAt);
    }

    @GetMapping("/company/{companyId}")
    public OperationalReportResponse getCompanyReport(
            @PathVariable UUID companyId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt
    ) {
        return operationalReportService.getCompanyReport(companyId, startAt, endAt);
    }
}