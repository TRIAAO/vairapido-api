package com.vairapido.api.service;

import com.vairapido.api.dto.report.OperationalReportResponse;
import com.vairapido.api.entity.enums.TicketAuditAction;
import com.vairapido.api.entity.enums.TicketStatus;
import com.vairapido.api.repository.TicketAuditLogRepository;
import com.vairapido.api.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OperationalReportService {

    private final TicketRepository ticketRepository;
    private final TicketAuditLogRepository ticketAuditLogRepository;

    public OperationalReportService(
            TicketRepository ticketRepository,
            TicketAuditLogRepository ticketAuditLogRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketAuditLogRepository = ticketAuditLogRepository;
    }

    @Transactional(readOnly = true)
    public OperationalReportResponse getGlobalReport() {
        long totalTickets = ticketRepository.count();

        long validTickets = ticketRepository.countByStatus(TicketStatus.VALID);
        long usedTickets = ticketRepository.countByStatus(TicketStatus.USED);
        long cancelledTickets = ticketRepository.countByStatus(TicketStatus.CANCELLED);

        long publicValidations = ticketAuditLogRepository.countByAction(
                TicketAuditAction.PUBLIC_VALIDATION
        );

        long successfulPublicValidations = ticketAuditLogRepository.countByActionAndSuccess(
                TicketAuditAction.PUBLIC_VALIDATION,
                true
        );

        long failedPublicValidations = ticketAuditLogRepository.countByActionAndSuccess(
                TicketAuditAction.PUBLIC_VALIDATION,
                false
        );

        long boardingAttempts = ticketAuditLogRepository.countByAction(
                TicketAuditAction.BOARDING
        );

        long successfulBoardings = ticketAuditLogRepository.countByActionAndSuccess(
                TicketAuditAction.BOARDING,
                true
        );

        long failedBoardings = ticketAuditLogRepository.countByActionAndSuccess(
                TicketAuditAction.BOARDING,
                false
        );

        return buildResponse(
                null,
                "GLOBAL",
                totalTickets,
                validTickets,
                usedTickets,
                cancelledTickets,
                publicValidations,
                successfulPublicValidations,
                failedPublicValidations,
                boardingAttempts,
                successfulBoardings,
                failedBoardings
        );
    }

    @Transactional(readOnly = true)
    public OperationalReportResponse getCompanyReport(UUID companyId) {
        long totalTickets = ticketRepository.countByBooking_Trip_TransportCompany_Id(
                companyId
        );

        long validTickets = ticketRepository.countByStatusAndBooking_Trip_TransportCompany_Id(
                TicketStatus.VALID,
                companyId
        );

        long usedTickets = ticketRepository.countByStatusAndBooking_Trip_TransportCompany_Id(
                TicketStatus.USED,
                companyId
        );

        long cancelledTickets = ticketRepository.countByStatusAndBooking_Trip_TransportCompany_Id(
                TicketStatus.CANCELLED,
                companyId
        );

        long publicValidations = ticketAuditLogRepository
                .countByActionAndTicket_Booking_Trip_TransportCompany_Id(
                        TicketAuditAction.PUBLIC_VALIDATION,
                        companyId
                );

        long successfulPublicValidations = ticketAuditLogRepository
                .countByActionAndSuccessAndTicket_Booking_Trip_TransportCompany_Id(
                        TicketAuditAction.PUBLIC_VALIDATION,
                        true,
                        companyId
                );

        long failedPublicValidations = ticketAuditLogRepository
                .countByActionAndSuccessAndTicket_Booking_Trip_TransportCompany_Id(
                        TicketAuditAction.PUBLIC_VALIDATION,
                        false,
                        companyId
                );

        long boardingAttempts = ticketAuditLogRepository
                .countByActionAndTicket_Booking_Trip_TransportCompany_Id(
                        TicketAuditAction.BOARDING,
                        companyId
                );

        long successfulBoardings = ticketAuditLogRepository
                .countByActionAndSuccessAndTicket_Booking_Trip_TransportCompany_Id(
                        TicketAuditAction.BOARDING,
                        true,
                        companyId
                );

        long failedBoardings = ticketAuditLogRepository
                .countByActionAndSuccessAndTicket_Booking_Trip_TransportCompany_Id(
                        TicketAuditAction.BOARDING,
                        false,
                        companyId
                );

        return buildResponse(
                companyId,
                "COMPANY",
                totalTickets,
                validTickets,
                usedTickets,
                cancelledTickets,
                publicValidations,
                successfulPublicValidations,
                failedPublicValidations,
                boardingAttempts,
                successfulBoardings,
                failedBoardings
        );
    }

    private OperationalReportResponse buildResponse(
            UUID companyId,
            String scope,
            long totalTickets,
            long validTickets,
            long usedTickets,
            long cancelledTickets,
            long publicValidations,
            long successfulPublicValidations,
            long failedPublicValidations,
            long boardingAttempts,
            long successfulBoardings,
            long failedBoardings
    ) {
        long suspiciousAttempts = failedPublicValidations + failedBoardings;

        return new OperationalReportResponse()
                .setCompanyId(companyId)
                .setScope(scope)

                .setTotalTickets(totalTickets)
                .setValidTickets(validTickets)
                .setUsedTickets(usedTickets)
                .setCancelledTickets(cancelledTickets)

                .setPublicValidations(publicValidations)
                .setSuccessfulPublicValidations(successfulPublicValidations)
                .setFailedPublicValidations(failedPublicValidations)

                .setBoardingAttempts(boardingAttempts)
                .setSuccessfulBoardings(successfulBoardings)
                .setFailedBoardings(failedBoardings)

                .setSuspiciousAttempts(suspiciousAttempts)
                .setGeneratedAt(LocalDateTime.now());
    }
}