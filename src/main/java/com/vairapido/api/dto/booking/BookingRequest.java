package com.vairapido.api.dto.booking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.vairapido.api.entity.enums.PassengerFareType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class BookingRequest {

    @NotNull(message = "A viagem é obrigatória.")
    private UUID tripId;

    @NotNull(message = "O passageiro é obrigatório.")
    private UUID passengerId;

    @NotNull(message = "O número da poltrona é obrigatório.")
    @Min(value = 1, message = "A poltrona deve ser maior que zero.")
    @Max(value = 99, message = "A poltrona deve ter no máximo 99.")
    private Integer seatNumber;

    private PassengerFareType passengerFareType;

    public UUID getTripId() {
        return tripId;
    }

    public BookingRequest setTripId(UUID tripId) {
        this.tripId = tripId;
        return this;
    }

    public UUID getPassengerId() {
        return passengerId;
    }

    public BookingRequest setPassengerId(UUID passengerId) {
        this.passengerId = passengerId;
        return this;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public PassengerFareType getPassengerFareType() {
        return passengerFareType;
    }

    public BookingRequest setPassengerFareType(PassengerFareType passengerFareType) {
        this.passengerFareType = passengerFareType;
        return this;
    }

    public BookingRequest setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
        return this;
    }
}