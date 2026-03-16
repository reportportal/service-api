package com.epam.reportportal.auth.config;

import com.epam.reportportal.auth.integration.validator.request.SamlUpdateAuthRequestValidator;
import com.epam.reportportal.auth.integration.validator.request.param.provider.ParamNamesProvider;
import com.epam.reportportal.auth.integration.validator.request.param.provider.SamlRequiredParamNamesProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthIntegrationValidatorConfig {

  @Bean
  public ParamNamesProvider samlParamNamesProvider() {
    return new SamlRequiredParamNamesProvider();
  }

  @Bean
  public SamlUpdateAuthRequestValidator samlUpdateAuthRequestValidator() {
    return new SamlUpdateAuthRequestValidator(samlParamNamesProvider());
  }

}
