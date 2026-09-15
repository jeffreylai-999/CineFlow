package com.cineflow.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class StaffIdentityMigrationTest {

	@Test
	void productionMigrationDoesNotSeedBookingStaff() throws IOException {
		assertThat(read("/db/migration/V2__staff_identity.sql")).doesNotContain("booking.staff");
		assertThat(read("/db/dev/V2_1__booking_staff_fixture.sql")).contains("booking.staff");
	}

	private static String read(String path) throws IOException {
		try (InputStream in = StaffIdentityMigrationTest.class.getResourceAsStream(path)) {
			assertThat(in).as(path).isNotNull();
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
