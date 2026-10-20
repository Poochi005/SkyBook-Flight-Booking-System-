package com.flightbooking.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** UPI / CARD / NETBANKING */
    @Column(nullable = false, length = 20)
    private String method;

    /** SUCCESS / REFUNDED */
    @Column(nullable = false, length = 15)
    private String status;

    @Column(name = "paid_on", nullable = false)
    private LocalDateTime paidOn = LocalDateTime.now();

    public Payment() { }

    public Payment(Booking booking, BigDecimal amount, String method, String status) {
        this.booking = booking;
        this.amount = amount;
        this.method = method;
        this.status = status;
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPaidOn() { return paidOn; }
    public void setPaidOn(LocalDateTime paidOn) { this.paidOn = paidOn; }
}
