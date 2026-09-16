package com.cineflow.booking;

import java.util.List;

public record CustomerShowtimeDateGroup(String cinemaDate, List<CustomerShowtimeSummary> showtimes) {
}
