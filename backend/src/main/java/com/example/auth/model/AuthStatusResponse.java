package com.example.auth.model;

public record AuthStatusResponse(boolean authenticated, String username, String message) {
}
