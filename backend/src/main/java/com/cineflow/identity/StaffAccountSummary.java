package com.cineflow.identity;

public record StaffAccountSummary(long id, String username, StaffRole role, boolean active) {
}
