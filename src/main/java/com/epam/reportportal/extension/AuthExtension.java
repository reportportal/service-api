package com.epam.reportportal.extension;

import com.epam.reportportal.auth.integration.handler.GetAuthIntegrationStrategy;
import com.epam.reportportal.auth.integration.handler.impl.strategy.AuthIntegrationStrategy;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.authentication.AuthenticationProvider;

/**
 * Extension point for authentication.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public interface AuthExtension extends ReportPortalExtensionPoint {

  /**
   * Returns authentication provider.
   *
   * @return {@link AuthenticationProvider}
   */
  AuthenticationProvider getAuthenticationProvider();

  default Optional<Map<String, Object>> getAuthProviderInfo() {
    return Optional.empty();
  }

  default Optional<AuthIntegrationStrategy> getStrategy() {
    return Optional.empty();
  }

  default Optional<GetAuthIntegrationStrategy> getSamlStrategy() {
    return Optional.empty();
  }
}
