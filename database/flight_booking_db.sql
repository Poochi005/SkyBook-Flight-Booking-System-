-- SkyBook - Flight Ticket Booking System
-- Optional: the application creates these tables automatically (spring.jpa.hibernate.ddl-auto=update).
-- Run this script only if you want to create the database manually in MySQL Workbench.

CREATE DATABASE IF NOT EXISTS flight_booking_db;
USE flight_booking_db;

CREATE TABLE IF NOT EXISTS users (
    user_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    email     VARCHAR(100) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,            -- BCrypt hash
    phone     VARCHAR(15),
    role      VARCHAR(20)  NOT NULL DEFAULT 'PASSENGER'   -- PASSENGER or ADMIN
);

CREATE TABLE IF NOT EXISTS flights (
    flight_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    flight_number    VARCHAR(10)  NOT NULL UNIQUE,
    airline          VARCHAR(50)  NOT NULL,
    source           VARCHAR(50)  NOT NULL,
    destination      VARCHAR(50)  NOT NULL,
    departure_time   DATETIME     NOT NULL,
    arrival_time     DATETIME     NOT NULL,
    price            DECIMAL(10,2) NOT NULL,
    total_seats      INT NOT NULL,
    available_seats  INT NOT NULL,
    INDEX idx_flight_search (source, destination, departure_time)
);

CREATE TABLE IF NOT EXISTS bookings (
    booking_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    pnr           VARCHAR(6)  NOT NULL UNIQUE,
    user_id       BIGINT      NOT NULL,
    flight_id     BIGINT      NOT NULL,
    booking_date  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount  DECIMAL(10,2) NOT NULL,
    status        VARCHAR(15) NOT NULL DEFAULT 'CONFIRMED',   -- CONFIRMED or CANCELLED
    FOREIGN KEY (user_id)   REFERENCES users(user_id),
    FOREIGN KEY (flight_id) REFERENCES flights(flight_id)
);

CREATE TABLE IF NOT EXISTS passengers (
    passenger_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id    BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    age           INT          NOT NULL,
    gender        VARCHAR(10),
    seat_number   VARCHAR(5)   NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id  BIGINT        NOT NULL UNIQUE,
    amount      DECIMAL(10,2) NOT NULL,
    method      VARCHAR(20)   NOT NULL,          -- UPI / CARD / NETBANKING
    status      VARCHAR(15)   NOT NULL,          -- SUCCESS / REFUNDED
    paid_on     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

-- Search query used by the application
-- SELECT * FROM flights
-- WHERE source = 'Chennai' AND destination = 'Delhi'
--   AND DATE(departure_time) = '2026-08-15' AND available_seats > 0;
