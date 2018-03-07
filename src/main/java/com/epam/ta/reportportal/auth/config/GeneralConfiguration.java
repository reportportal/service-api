package com.epam.ta.reportportal.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;

import javax.sql.DataSource;

@Configuration
public class GeneralConfiguration {

	@Autowired
	private DataSource dataSource;

	@Bean
	public ClientRegistrationService jdbcClientRegistrationService() {
		return new JdbcClientDetailsService(dataSource);
	}
}
