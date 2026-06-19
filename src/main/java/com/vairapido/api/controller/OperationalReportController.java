package com.vairapido.api.controller;

import com.vairapido.api.dto.report.OperationalReportResponse;
import com.vairapido.api.dto.report.OperationalTicketReportItemResponse;
import com.vairapido.api.dto.ticketaudit.TicketAuditLogResponse;
import com.vairapido.api.service.OperationalReportCsvService;
import com.vairapido.api.service.OperationalReportDetailService;
import com.vairapido.api.service.OperationalReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports/operational")
public class OperationalReportController {

    private static final DateTimeFormatter FILE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final OperationalReportService operationalReportService;
    private final OperationalReportCsvService operationalReportCsvService;
    private final OperationalReportDetailService operationalReportDetailService;

    public OperationalReportController(
            OperationalReportService operationalReportService,
            OperationalReportCsvService operationalReportCsvService,
            OperationalReportDetailService operationalReportDetailService
    ) {
        this.operationalReportService = operationalReportService;
        this.operationalReportCsvService = operationalReportCsvService;
        this.operationalReportDetailService = operationalReportDetailService;
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

    @GetMapping("/tickets")
    public List<OperationalTicketReportItemResponse> getGlobalTickets(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt
    ) {
        return operationalReportDetailService.findGlobalTickets(startAt, endAt);
    }

    @GetMapping("/audit-logs")
    public List<TicketAuditLogResponse> getGlobalAuditLogs(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt
    ) {
        return operationalReportDetailService.findGlobalAuditLogs(startAt, endAt);
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

    @GetMapping("/company/{companyId}/tickets")
    public List<OperationalTicketReportItemResponse> getCompanyTickets(
            @PathVariable UUID companyId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt
    ) {
        return operationalReportDetailService.findCompanyTickets(companyId, startAt, endAt);
    }

    @GetMapping("/company/{companyId}/audit-logs")
    public List<TicketAuditLogResponse> getCompanyAuditLogs(
            @PathVariable UUID companyId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt
    ) {
        return operationalReportDetailService.findCompanyAuditLogs(companyId, startAt, endAt);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportGlobalReportCsv(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt
    ) {
        OperationalReportResponse report =
                operationalReportService.getGlobalReport(startAt, endAt);

        byte[] csv = operationalReportCsvService.generateCsv(report);

        return buildCsvResponse(
                csv,
                "relatorio-operacional-global-" + nowForFileName() + ".csv"
        );
    }

    @GetMapping("/company/{companyId}/export/csv")
    public ResponseEntity<byte[]> exportCompanyReportCsv(
            @PathVariable UUID companyId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt
    ) {
        OperationalReportResponse report =
                operationalReportService.getCompanyReport(companyId, startAt, endAt);

        byte[] csv = operationalReportCsvService.generateCsv(report);

        return buildCsvResponse(
                csv,
                "relatorio-operacional-empresa-" + companyId + "-" + nowForFileName() + ".csv"
        );
    }

    private ResponseEntity<byte[]> buildCsvResponse(byte[] csv, String fileName) {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv"))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileName)
                                .build()
                                .toString()
                )
                .body(csv);
    }

    private String nowForFileName() {
        return LocalDateTime.now().format(FILE_DATE_FORMATTER);
    }
}