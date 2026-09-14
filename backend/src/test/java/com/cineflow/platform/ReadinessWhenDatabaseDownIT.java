package com.cineflow.platform;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.cineflow.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
		"spring.datasource.hikari.connection-timeout=2000",
		"spring.datasource.hikari.validation-timeout=1000"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReadinessWhenDatabaseDownIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	PostgreSQLContainer postgres;

	@Test
	void readinessReportsDownWhenDatabaseStops() throws Exception {
		postgres.stop();
		mockMvc.perform(get("/actuator/health/readiness").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.status").value("DOWN"));
	}
}
