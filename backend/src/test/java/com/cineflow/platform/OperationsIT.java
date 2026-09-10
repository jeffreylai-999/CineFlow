package com.cineflow.platform;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cineflow.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class OperationsIT {

	@Autowired
	MockMvc mockMvc;

	@Test
	void readinessReportsUpWhenDatabaseIsReachable() throws Exception {
		mockMvc.perform(get("/actuator/health/readiness").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void livenessReportsUp() throws Exception {
		mockMvc.perform(get("/actuator/health/liveness").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void openApiDocumentDescribesMoviesEndpoint() throws Exception {
		mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.paths./api/movies.get").exists())
			.andExpect(jsonPath("$.info.title", containsString("CineFlow")));
	}
}
