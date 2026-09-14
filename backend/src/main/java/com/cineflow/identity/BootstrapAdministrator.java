package com.cineflow.identity;

import java.time.Clock;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class BootstrapAdministrator implements ApplicationRunner {

	private final StaffAccountRepository staffAccounts;
	private final BootstrapAdministratorProperties properties;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	BootstrapAdministrator(
			StaffAccountRepository staffAccounts,
			BootstrapAdministratorProperties properties,
			PasswordEncoder passwordEncoder,
			Clock clock) {
		this.staffAccounts = staffAccounts;
		this.properties = properties;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (staffAccounts.countByRole(StaffRole.ADMINISTRATOR) > 0) {
			return;
		}
		String username = properties.username();
		String password = properties.password();
		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			throw new IllegalStateException(
					"Bootstrap Administrator credentials are required when no Administrator exists");
		}
		staffAccounts.save(new StaffAccountEntity(
				username,
				passwordEncoder.encode(password),
				StaffRole.ADMINISTRATOR,
				clock.instant()));
	}
}
