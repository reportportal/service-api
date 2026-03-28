package com.epam.reportportal.extension;

import com.epam.reportportal.auth.integration.handler.GetAuthIntegrationStrategy;
import com.epam.reportportal.auth.integration.handler.impl.strategy.AuthIntegrationStrategy;
import com.epam.reportportal.auth.model.settings.OAuthRegistrationResource;
import com.epam.reportportal.auth.oauth.OAuthProvider;
import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistration;
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

  default Optional<GetAuthIntegrationStrategy> getListIntegrationStrategy() {
    return Optional.empty();
  }

  /**
   * Returns the auth integration type name (e.g. "saml", "ldap") this plugin handles, if any.
   * Used to register strategy mappings dynamically from the plugin.
   */
  default Optional<String> getAuthIntegrationType() {
    return Optional.empty();
  }

  /**
   * Returns the OAuth2 provider contributed by this plugin, if any.
   * Used to plug in OAuth2 login user services (e.g. GitHub).
   */
  default Optional<OAuthProvider> getOAuthProvider() {
    return Optional.empty();
  }

  /**
   * Creates an OAuthRegistration entity for the given provider ID and configuration, if this plugin handles it.
   * Returns empty if this plugin does not handle the given provider ID.
   */
  default Optional<OAuthRegistration> fillOAuthRegistration(String oauthProviderId,
      OAuthRegistrationResource registrationResource, String pathValue) {
    return Optional.empty();
  }

}
