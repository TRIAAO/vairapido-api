package com.vairapido.api.controller;

import com.vairapido.api.dto.report.FinancialBookingReportItemResponse;
import com.vairapido.api.dto.report.FinancialReportResponse;
import com.vairapido.api.service.FinancialReportCsvService;
import com.vairapido.api.service.FinancialReportDetailCsvService;
import com.vairapido.api.service.FinancialReportDetailService;
import com.vairapido.api.service.FinancialReportService;
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
@RequestMapping("/api/v1/reports/financial")
public class FinancialReportController {

    private static final DateTimeFormatter FILE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final FinancialReportService financialReportService;
    private final FinancialReportCsvService financialReportCsvService;
    private final FinancialReportDetailService financialReportDetailService;
    private final FinancialReportDetailCsvService financialReportDetailCsvService;

    public FinancialReportController(
            FinancialReportService financialReportService,
            FinancialReportCsvService financialReportCsvService,
            FinancialReportDetailService financialReportDetailService,
            FinancialReportDetailCsvService financialReportDetailCsvService
    ) {
        this.financialReportService = financialReportService;
        this.financialReportCsvService = financialReportCsvService;
        this.financialReportDetailService = financialReportDetailService;
        this.financialReportDetailCsvService = financialReportDetailCsvService;
    }

    @GetMapping
    public FinancialReportResponse getGlobalFinancialReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt,

            @RequestParam(required = false, defaultValue = "BRL")
            String currency
    ) {
        return financialReportService.getGlobalReport(
                startAt,
                endAt,
                currency
        );
    }

    @GetMapping("/bookings")
    public List<FinancialBookingReportItemResponse> getGlobalFinancialBookings(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt,

            @RequestParam(required = false, defaultValue = "BRL")
            String currency
    ) {
        return financialReportDetailService.findGlobalBookings(
                startAt,
                endAt,
                currency
        );
    }

    @GetMapping("/company/{companyId}")
    public FinancialReportResponse getCompanyFinancialReport(
            @PathVariable UUID companyId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt,

            @RequestParam(required = false, defaultValue = "BRL")
            String currency
    ) {
        return financialReportService.getCompanyReport(
                companyId,
                startAt,
                endAt,
                currency
        );
    }

    @GetMapping("/company/{companyId}/bookings")
    public List<FinancialBookingReportItemResponse> getCompanyFinancialBookings(
            @PathVariable UUID companyId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt,

            @RequestParam(required = false, defaultValue = "BRL")
            String currency
    ) {
        return financialReportDetailService.findCompanyBookings(
                companyId,
                startAt,
                endAt,
                currency
        );
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportGlobalFinancialReportCsv(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt,

            @RequestParam(required = false, defaultValue = "BRL")
            String currency
    ) {
        FinancialReportResponse report = financialReportService.getGlobalReport(
                startAt,
                endAt,
                currency
        );

        byte[] csv = financialReportCsvService.generateCsv(report);

        return buildCsvResponse(
                csv,
                "relatorio-financeiro-global-" + nowForFileName() + ".csv"
        );
    }

    @GetMapping("/company/{companyId}/export/csv")
    public ResponseEntity<byte[]> exportCompanyFinancialReportCsv(
            @PathVariable UUID companyId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt,

            @RequestParam(required = false, defaultValue = "BRL")
            String currency
    ) {
        FinancialReportResponse report = financialReportService.getCompanyReport(
                companyId,
                startAt,
                endAt,
                currency
        );

        byte[] csv = financialReportCsvService.generateCsv(report);

        return buildCsvResponse(
                csv,
                "relatorio-financeiro-empresa-" + companyId + "-" + nowForFileName() + ".csv"
        );
    }

    @GetMapping("/bookings/export/csv")
    public ResponseEntity<byte[]> exportGlobalFinancialBookingsCsv(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt,

            @RequestParam(required = false, defaultValue = "BRL")
            String currency
    ) {
        List<FinancialBookingReportItemResponse> bookings =
                financialReportDetailService.findGlobalBookings(
                        startAt,
                        endAt,
                        currency
                );

        byte[] csv = financialReportDetailCsvService.generateBookingsCsv(bookings);

        return buildCsvResponse(
                csv,
                "relatorio-financeiro-reservas-global-" + nowForFileName() + ".csv"
        );
    }

    @GetMapping("/company/{companyId}/bookings/export/csv")
    public ResponseEntity<byte[]> exportCompanyFinancialBookingsCsv(
            @PathVariable UUID companyId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt,

            @RequestParam(required = false, defaultValue = "BRL")
            String currency
    ) {
        List<FinancialBookingReportItemResponse> bookings =
                financialReportDetailService.findCompanyBookings(
                        companyId,
                        startAt,
                        endAt,
                        currency
                );

        byte[] csv = financialReportDetailCsvService.generateBookingsCsv(bookings);

        return buildCsvResponse(
                csv,
                "relatorio-financeiro-reservas-empresa-" + companyId + "-" + nowForFileName() + ".csv"
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