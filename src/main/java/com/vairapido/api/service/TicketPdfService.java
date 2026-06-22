package com.vairapido.api.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vairapido.api.entity.Booking;
import com.vairapido.api.entity.Passenger;
import com.vairapido.api.entity.Ticket;
import com.vairapido.api.entity.TransportCompany;
import com.vairapido.api.entity.TravelRoute;
import com.vairapido.api.entity.Trip;
import com.vairapido.api.entity.enums.PassengerDocumentType;
import com.vairapido.api.exception.NotFoundException;
import com.vairapido.api.repository.TicketRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class TicketPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    private final TicketRepository ticketRepository;
    private final DocumentValidatorService documentValidatorService;

    public TicketPdfService(
            TicketRepository ticketRepository,
            DocumentValidatorService documentValidatorService
    ) {
        this.ticketRepository = ticketRepository;
        this.documentValidatorService = documentValidatorService;
    }

    @Transactional(readOnly = true)
    public byte[] generateTicketPdf(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Bilhete não encontrado."));

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            Booking booking = ticket.getBooking();
            Trip trip = booking.getTrip();
            Passenger passenger = booking.getPassenger();
            TransportCompany company = trip.getTransportCompany();
            TravelRoute route = trip.getRoute();

            PDImageXObject qrImage = createQrImage(document, ticket.getValidationUrl());

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                drawBackground(content);
                drawHeader(content);
                drawStatusBox(content, ticket);
                drawMainTicketCard(content);
                drawSuccessBanner(content);
                drawTicketMeta(content, ticket, booking);
                drawRouteSection(content, route, trip, booking);
                drawPassengerCard(content, passenger);
                drawQrCard(content, qrImage);
                drawSummaryStrip(content, booking, trip, company, route);
                drawBoardingWarning(content);
                drawSecurityFooter(content, ticket);
                drawBottomBrand(content);
            }

            document.save(outputStream);
            return outputStream.toByteArray();

        } catch (IOException | WriterException exception) {
            throw new IllegalStateException("Erro ao gerar PDF do bilhete.", exception);
        }
    }

    private PDImageXObject createQrImage(PDDocument document, String validationUrl)
            throws WriterException, IOException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                validationUrl,
                BarcodeFormat.QR_CODE,
                320,
                320
        );

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        return LosslessFactory.createFromImage(document, bufferedImage);
    }

    private void drawBackground(PDPageContentStream content) throws IOException {
        content.setNonStrokingColor(255, 255, 255);
        content.addRect(0, 0, 595, 842);
        content.fill();
    }

    private void drawHeader(PDPageContentStream content) throws IOException {
        content.setNonStrokingColor(13, 27, 42);
        content.addRect(0, 735, 595, 107);
        content.fill();

        content.setNonStrokingColor(255, 193, 7);
        content.addRect(0, 730, 595, 5);
        content.fill();

        content.setNonStrokingColor(255, 193, 7);
        content.addRect(45, 775, 42, 42);
        content.fill();

        content.setNonStrokingColor(13, 27, 42);
        content.addRect(55, 787, 22, 18);
        content.fill();

        content.beginText();
        content.setNonStrokingColor(255, 255, 255);
        content.setFont(PDType1Font.HELVETICA_BOLD, 28);
        content.newLineAtOffset(100, 795);
        content.showText("Vai");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(255, 193, 7);
        content.setFont(PDType1Font.HELVETICA_BOLD, 28);
        content.newLineAtOffset(145, 795);
        content.showText("Rapido");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(226, 232, 240);
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(102, 775);
        content.showText("Bilhete digital de passagem");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(255, 255, 255);
        content.setFont(PDType1Font.HELVETICA_BOLD, 13);
        content.newLineAtOffset(410, 805);
        content.showText("BILHETE DE PASSAGEM");
        content.endText();
    }

    private void drawStatusBox(PDPageContentStream content, Ticket ticket) throws IOException {
        content.setNonStrokingColor(34, 197, 94);
        content.addRect(410, 760, 120, 38);
        content.fill();

        content.beginText();
        content.setNonStrokingColor(255, 255, 255);
        content.setFont(PDType1Font.HELVETICA_BOLD, 20);
        content.newLineAtOffset(435, 773);
        content.showText(ticket.getStatus().name());
        content.endText();

        content.beginText();
        content.setNonStrokingColor(226, 232, 240);
        content.setFont(PDType1Font.HELVETICA, 9);
        content.newLineAtOffset(405, 745);
        content.showText("Apresente este bilhete no embarque");
        content.endText();
    }

    private void drawMainTicketCard(PDPageContentStream content) throws IOException {
        content.setNonStrokingColor(255, 255, 255);
        content.addRect(28, 55, 539, 650);
        content.fill();

        content.setStrokingColor(226, 232, 240);
        content.addRect(28, 55, 539, 650);
        content.stroke();
    }

    private void drawSuccessBanner(PDPageContentStream content) throws IOException {
        content.setNonStrokingColor(240, 253, 244);
        content.addRect(45, 655, 505, 40);
        content.fill();

        content.setStrokingColor(187, 247, 208);
        content.addRect(45, 655, 505, 40);
        content.stroke();

        content.beginText();
        content.setNonStrokingColor(22, 163, 74);
        content.setFont(PDType1Font.HELVETICA_BOLD, 16);
        content.newLineAtOffset(65, 678);
        content.showText("Bilhete confirmado");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(22, 163, 74);
        content.setFont(PDType1Font.HELVETICA, 10);
        content.newLineAtOffset(65, 664);
        content.showText("Apresente este bilhete no momento do embarque.");
        content.endText();
    }

    private void drawTicketMeta(
            PDPageContentStream content,
            Ticket ticket,
            Booking booking
    ) throws IOException {
        float y = 615;

        drawMetaItem(content, "CODIGO DO BILHETE", ticket.getTicketCode(), 45, y, 160);
        drawMetaItem(content, "CODIGO DA RESERVA", booking.getBookingCode(), 210, y, 130);
        drawMetaItem(content, "DATA DA EMISSAO", formatDateTime(ticket.getIssuedAt()), 355, y, 110);
        drawMetaItem(content, "TIPO", "Convencional", 470, y, 70);
    }

    private void drawRouteSection(
            PDPageContentStream content,
            TravelRoute route,
            Trip trip,
            Booking booking
    ) throws IOException {
        float x = 45;
        float y = 495;
        float width = 505;
        float height = 105;

        content.setNonStrokingColor(248, 250, 252);
        content.addRect(x, y, width, height);
        content.fill();

        content.setStrokingColor(226, 232, 240);
        content.addRect(x, y, width, height);
        content.stroke();

        content.beginText();
        content.setNonStrokingColor(30, 64, 175);
        content.setFont(PDType1Font.HELVETICA_BOLD, 8);
        content.newLineAtOffset(65, 570);
        content.showText("ORIGEM");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(15, 23, 42);
        content.setFont(PDType1Font.HELVETICA_BOLD, 24);
        content.newLineAtOffset(65, 545);
        content.showText(limitText(formatCityState(route.getOriginCity(), route.getOriginState()), 22));
        content.endText();

        content.beginText();
        content.setNonStrokingColor(71, 85, 105);
        content.setFont(PDType1Font.HELVETICA, 10);
        content.newLineAtOffset(65, 528);
        content.showText(limitText(safeText(route.getOriginTerminal()), 30));
        content.endText();

        content.beginText();
        content.setNonStrokingColor(30, 64, 175);
        content.setFont(PDType1Font.HELVETICA_BOLD, 8);
        content.newLineAtOffset(340, 570);
        content.showText("DESTINO");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(15, 23, 42);
        content.setFont(PDType1Font.HELVETICA_BOLD, 24);
        content.newLineAtOffset(340, 545);
        content.showText(limitText(formatCityState(route.getDestinationCity(), route.getDestinationState()), 22));
        content.endText();

        content.beginText();
        content.setNonStrokingColor(71, 85, 105);
        content.setFont(PDType1Font.HELVETICA, 10);
        content.newLineAtOffset(340, 528);
        content.showText(limitText(safeText(route.getDestinationTerminal()), 30));
        content.endText();

        content.setNonStrokingColor(13, 27, 42);
        content.addRect(279, 538, 34, 34);
        content.fill();

        content.beginText();
        content.setNonStrokingColor(255, 193, 7);
        content.setFont(PDType1Font.HELVETICA_BOLD, 18);
        content.newLineAtOffset(290, 548);
        content.showText(">");
        content.endText();

        content.setNonStrokingColor(239, 246, 255);
        content.addRect(x, y, width, 35);
        content.fill();

        drawRouteInfo(content, "DATA DA VIAGEM", formatDate(trip), 65, 510);
        drawRouteInfo(content, "HORARIO", formatTime(trip), 200, 510);
        drawRouteInfo(content, "POLTRONA", String.valueOf(booking.getSeatNumber()), 330, 510);
        drawRouteInfo(content, "CHEGADA", formatDateTime(trip.getArrivalAt()), 430, 510);
    }

    private void drawPassengerCard(
            PDPageContentStream content,
            Passenger passenger
    ) throws IOException {
        float x = 45;
        float y = 270;
        float width = 235;
        float height = 205;

        content.setNonStrokingColor(255, 255, 255);
        content.addRect(x, y, width, height);
        content.fill();

        content.setStrokingColor(226, 232, 240);
        content.addRect(x, y, width, height);
        content.stroke();

        content.setNonStrokingColor(13, 27, 42);
        content.addRect(x, y + height - 35, width, 35);
        content.fill();

        content.beginText();
        content.setNonStrokingColor(255, 255, 255);
        content.setFont(PDType1Font.HELVETICA_BOLD, 11);
        content.newLineAtOffset(x + 18, y + height - 22);
        content.showText("PASSAGEIRO");
        content.endText();

        PassengerDocumentType documentType = getPassengerDocumentType(passenger);
        String documentLabel = documentValidatorService.label(documentType);
        String documentNumber = documentValidatorService.normalize(documentType, passenger.getDocumentNumber());

        drawPassengerLine(content, "NOME COMPLETO", passenger.getFullName(), x + 18, y + 145);
        drawPassengerLine(content, documentLabel, documentNumber, x + 18, y + 100);
        drawPassengerLine(content, "TELEFONE / WHATSAPP", passenger.getWhatsapp(), x + 18, y + 55);
        drawPassengerLine(content, "TIPO DE PASSAGEIRO", "Adulto", x + 18, y + 15);
    }

    private void drawQrCard(
            PDPageContentStream content,
            PDImageXObject qrImage
    ) throws IOException {
        float x = 300;
        float y = 270;
        float width = 250;
        float height = 205;

        content.setNonStrokingColor(255, 255, 255);
        content.addRect(x, y, width, height);
        content.fill();

        content.setStrokingColor(226, 232, 240);
        content.addRect(x, y, width, height);
        content.stroke();

        content.setNonStrokingColor(13, 27, 42);
        content.addRect(x, y + height - 35, width, 35);
        content.fill();

        content.beginText();
        content.setNonStrokingColor(255, 255, 255);
        content.setFont(PDType1Font.HELVETICA_BOLD, 11);
        content.newLineAtOffset(x + 18, y + height - 22);
        content.showText("QR CODE DE VALIDACAO");
        content.endText();

        content.drawImage(qrImage, x + 57, y + 55, 135, 135);

        content.beginText();
        content.setNonStrokingColor(15, 23, 42);
        content.setFont(PDType1Font.HELVETICA_BOLD, 10);
        content.newLineAtOffset(x + 68, y + 35);
        content.showText("QR Code de validacao");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(71, 85, 105);
        content.setFont(PDType1Font.HELVETICA, 8);
        content.newLineAtOffset(x + 50, y + 20);
        content.showText("Escaneie para consultar este bilhete.");
        content.endText();
    }

    private void drawSummaryStrip(
            PDPageContentStream content,
            Booking booking,
            Trip trip,
            TransportCompany company,
            TravelRoute route
    ) throws IOException {
        float x = 45;
        float y = 215;
        float width = 505;
        float height = 42;

        content.setNonStrokingColor(255, 255, 255);
        content.addRect(x, y, width, height);
        content.fill();

        content.setStrokingColor(226, 232, 240);
        content.addRect(x, y, width, height);
        content.stroke();

        drawSmallSummary(content, "EMPRESA", getCompanyName(company), x + 15, y + 14);
        drawSmallSummary(content, "TRECHO", formatCityState(route.getOriginCity(), route.getOriginState()) + " / " + formatCityState(route.getDestinationCity(), route.getDestinationState()), x + 125, y + 14);
        drawSmallSummary(content, "VALOR PAGO", formatMoney(booking), x + 280, y + 14);
        drawSmallSummary(content, "STATUS", booking.getStatus().name(), x + 410, y + 14);
    }

    private void drawBoardingWarning(PDPageContentStream content) throws IOException {
        float x = 45;
        float y = 125;
        float width = 505;
        float height = 72;

        content.setNonStrokingColor(255, 251, 235);
        content.addRect(x, y, width, height);
        content.fill();

        content.setStrokingColor(245, 158, 11);
        content.addRect(x, y, width, height);
        content.stroke();

        content.beginText();
        content.setNonStrokingColor(146, 64, 14);
        content.setFont(PDType1Font.HELVETICA_BOLD, 13);
        content.newLineAtOffset(x + 20, y + 48);
        content.showText("Atencao no embarque");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(120, 53, 15);
        content.setFont(PDType1Font.HELVETICA, 8);
        content.newLineAtOffset(x + 20, y + 32);
        content.showText("Apresente documento oficial com o mesmo nome e numero informado neste bilhete.");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(120, 53, 15);
        content.setFont(PDType1Font.HELVETICA, 8);
        content.newLineAtOffset(x + 20, y + 20);
        content.showText("Divergencia de nome ou documento pode impedir o embarque.");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(120, 53, 15);
        content.setFont(PDType1Font.HELVETICA, 8);
        content.newLineAtOffset(x + 20, y + 8);
        content.showText("Dados declarados pelo comprador/passageiro no momento da emissao.");
        content.endText();
    }

    private void drawSecurityFooter(
            PDPageContentStream content,
            Ticket ticket
    ) throws IOException {
        float x = 45;
        float y = 70;
        float width = 505;
        float height = 38;

        content.setNonStrokingColor(13, 27, 42);
        content.addRect(x, y, width, height);
        content.fill();

        content.beginText();
        content.setNonStrokingColor(255, 255, 255);
        content.setFont(PDType1Font.HELVETICA_BOLD, 9);
        content.newLineAtOffset(x + 18, y + 23);
        content.showText("BILHETE SEGURO");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(226, 232, 240);
        content.setFont(PDType1Font.HELVETICA, 7);
        content.newLineAtOffset(x + 18, y + 11);
        content.showText("QR Code unico e valido apenas para uma utilizacao no embarque.");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(255, 193, 7);
        content.setFont(PDType1Font.HELVETICA_BOLD, 8);
        content.newLineAtOffset(x + 280, y + 18);
        content.showText("Validacao: " + limitText(ticket.getValidationUrl(), 55));
        content.endText();
    }

    private void drawBottomBrand(PDPageContentStream content) throws IOException {
        content.beginText();
        content.setNonStrokingColor(13, 27, 42);
        content.setFont(PDType1Font.HELVETICA_BOLD, 11);
        content.newLineAtOffset(258, 35);
        content.showText("VaiRapido");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(100, 116, 139);
        content.setFont(PDType1Font.HELVETICA, 7);
        content.newLineAtOffset(242, 22);
        content.showText("VIAJE COM CONFIANCA");
        content.endText();
    }

    private void drawMetaItem(
            PDPageContentStream content,
            String label,
            String value,
            float x,
            float y,
            int maxLength
    ) throws IOException {
        content.beginText();
        content.setNonStrokingColor(71, 85, 105);
        content.setFont(PDType1Font.HELVETICA_BOLD, 7);
        content.newLineAtOffset(x, y + 17);
        content.showText(limitText(safeText(label), maxLength));
        content.endText();

        content.beginText();
        content.setNonStrokingColor(15, 23, 42);
        content.setFont(PDType1Font.HELVETICA_BOLD, 10);
        content.newLineAtOffset(x, y + 2);
        content.showText(limitText(safeText(value), maxLength));
        content.endText();
    }

    private void drawRouteInfo(
            PDPageContentStream content,
            String label,
            String value,
            float x,
            float y
    ) throws IOException {
        content.beginText();
        content.setNonStrokingColor(71, 85, 105);
        content.setFont(PDType1Font.HELVETICA_BOLD, 7);
        content.newLineAtOffset(x, y + 14);
        content.showText(label);
        content.endText();

        content.beginText();
        content.setNonStrokingColor(15, 23, 42);
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.newLineAtOffset(x, y);
        content.showText(limitText(safeText(value), 22));
        content.endText();
    }

    private void drawPassengerLine(
            PDPageContentStream content,
            String label,
            String value,
            float x,
            float y
    ) throws IOException {
        content.beginText();
        content.setNonStrokingColor(30, 64, 175);
        content.setFont(PDType1Font.HELVETICA_BOLD, 7);
        content.newLineAtOffset(x, y + 16);
        content.showText(limitText(safeText(label), 26));
        content.endText();

        content.beginText();
        content.setNonStrokingColor(15, 23, 42);
        content.setFont(PDType1Font.HELVETICA_BOLD, 10);
        content.newLineAtOffset(x, y);
        content.showText(limitText(safeText(value), 32));
        content.endText();

        content.setStrokingColor(226, 232, 240);
        content.moveTo(x, y - 8);
        content.lineTo(x + 200, y - 8);
        content.stroke();
    }

    private void drawSmallSummary(
            PDPageContentStream content,
            String label,
            String value,
            float x,
            float y
    ) throws IOException {
        content.beginText();
        content.setNonStrokingColor(30, 64, 175);
        content.setFont(PDType1Font.HELVETICA_BOLD, 6);
        content.newLineAtOffset(x, y + 13);
        content.showText(label);
        content.endText();

        content.beginText();
        content.setNonStrokingColor(15, 23, 42);
        content.setFont(PDType1Font.HELVETICA_BOLD, 8);
        content.newLineAtOffset(x, y);
        content.showText(limitText(safeText(value), 28));
        content.endText();
    }

    private PassengerDocumentType getPassengerDocumentType(Passenger passenger) {
        if (passenger == null || passenger.getDocumentType() == null) {
            return documentValidatorService.defaultDocumentType();
        }

        return passenger.getDocumentType();
    }

    private String getCompanyName(TransportCompany company) {
        if (company == null) {
            return "-";
        }

        if (company.getTradeName() != null && !company.getTradeName().isBlank()) {
            return company.getTradeName();
        }

        return safeText(company.getName());
    }

    private String formatLocation(String city, String state, String terminal) {
        StringBuilder builder = new StringBuilder();

        builder.append(safeText(city));

        if (state != null && !state.isBlank()) {
            builder.append(" - ").append(state.trim());
        }

        if (terminal != null && !terminal.isBlank()) {
            builder.append(" | ").append(terminal.trim());
        }

        return builder.toString();
    }

    private String formatCityState(String city, String state) {
        if (state == null || state.isBlank()) {
            return safeText(city);
        }

        return safeText(city) + " - " + state.trim();
    }

    private String formatDate(Trip trip) {
        if (trip == null || trip.getDepartureAt() == null) {
            return "-";
        }

        return trip.getDepartureAt().format(DATE_FORMATTER);
    }

    private String formatTime(Trip trip) {
        if (trip == null || trip.getDepartureAt() == null) {
            return "-";
        }

        return trip.getDepartureAt().format(TIME_FORMATTER);
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }

        return dateTime.format(DATE_TIME_FORMATTER);
    }

    private String formatMoney(Booking booking) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);

        if (booking == null || booking.getAmount() == null) {
            return "-";
        }

        return safeText(booking.getCurrency()) + " " + numberFormat.format(booking.getAmount());
    }

    private String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        return value
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("→", ">")
                .replace("–", "-")
                .replace("—", "-")
                .trim();
    }

    private String limitText(String value, int maxLength) {
        String safeValue = safeText(value);

        if (safeValue.length() <= maxLength) {
            return safeValue;
        }

        return safeValue.substring(0, maxLength - 3) + "...";
    }
}