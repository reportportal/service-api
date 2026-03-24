package com.epam.reportportal.auth.config;

import com.epam.reportportal.auth.integration.validator.request.AuthRequestValidator;
import com.epam.reportportal.auth.integration.validator.request.UpdateAuthRequestValidator;
import com.epam.reportportal.base.infrastructure.model.integration.auth.UpdateAuthRQ;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers auth request validators. Each auth plugin provides its own validation logic;
 * this bean is a no-op fallback used by plugins that do not need additional param validation.
 */
@Configuration
public class AuthIntegrationValidatorConfig {

  @Bean
  public AuthRequestValidator<UpdateAuthRQ> updateAuthRequestValidator() {
    return new UpdateAuthRequestValidator(Collections::emptyList);
  }
}
