package com.flightbooking.exception;

/** Business-rule violation (no seats left, already cancelled, duplicate e-mail ...). */
public class BookingException extends RuntimeException {
    public BookingException(String message) { super(message); }
}
