package com.vairapido.api.controller;

import com.vairapido.api.dto.dashboard.DashboardSummaryResponse;
import com.vairapido.api.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public DashboardSummaryResponse summary() {
        return service.getSummary();
    }

    @GetMapping("/company/{companyId}/summary")
    @PreAuthorize("@companyAccessService.canAccessCompany(#p0)")
    public DashboardSummaryResponse companySummary(
            @PathVariable UUID companyId
    ) {
        return service.getCompanySummary(companyId);
    }
}