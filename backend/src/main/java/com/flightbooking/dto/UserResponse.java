package com.flightbooking.dto;

import com.flightbooking.model.User;

public class UserResponse {
    private final Long userId;
    private final String name;
    private final String email;
    private final String role;

    public UserResponse(User u) {
        this.userId = u.getUserId();
        this.name = u.getName();
        this.email = u.getEmail();
        this.role = u.getRole();
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
