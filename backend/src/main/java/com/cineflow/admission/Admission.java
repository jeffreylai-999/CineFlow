package com.cineflow.admission;

public interface Admission {

	AdmissionResponse admit(long staffId, AdmitBookingRequest request);
}
