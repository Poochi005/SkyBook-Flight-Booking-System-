package com.flightbooking.dto;

import jakarta.validation.constraints.*;

public class PassengerDto {
    @NotBlank(message = "Passenger name is required")
    @Size(max = 100, message = "Passenger name is too long")
    private String name;

    @Min(value = 1, message = "Age must be between 1 and 120")
    @Max(value = 120, message = "Age must be between 1 and 120")
    private int age;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Seat number is required")
    @Pattern(regexp = "^[0-9]{1,2}[A-Za-z]$", message = "Seat must look like 12A")
    private String seatNumber;

    public PassengerDto() { }

    public PassengerDto(String name, int age, String gender, String seatNumber) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.seatNumber = seatNumber;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
}
