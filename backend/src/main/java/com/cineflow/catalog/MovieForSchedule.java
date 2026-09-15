package com.cineflow.catalog;

public record MovieForSchedule(long id, String title, int runtimeMinutes, boolean archived) {
}
