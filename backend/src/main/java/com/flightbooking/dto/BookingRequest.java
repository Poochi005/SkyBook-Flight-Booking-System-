package com.flightbooking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BookingRequest {
    @NotNull(message = "Flight is required")
    private Long flightId;

    @NotNull(message = "Payment method is required")
    @Pattern(regexp = "UPI|CARD|NETBANKING", message = "Payment method must be UPI, CARD or NETBANKING")
    private String paymentMethod;

    @NotEmpty(message = "At least one passenger is required")
    @Size(max = 9, message = "A booking can have at most 9 passengers")
    @Valid
    private List<PassengerDto> passengers;

    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public List<PassengerDto> getPassengers() { return passengers; }
    public void setPassengers(List<PassengerDto> passengers) { this.passengers = passengers; }
}
