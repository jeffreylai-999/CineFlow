package com.cineflow.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

	@GetMapping(value = {
			"/",
			"/{path:^(?!api|actuator|v3|swagger-ui|assets)[^\\.]*}",
			"/{path:^(?!api|actuator|v3|swagger-ui|assets)[^\\.]*}/**"
	})
	public String forwardSpaRoutes() {
		return "forward:/index.html";
	}
}
