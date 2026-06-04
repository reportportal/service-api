package com.epam.reportportal.auth;

import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.extension.AuthExtension;
import com.epam.reportportal.extension.common.ExtensionPoint;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Dynamically resolves AuthExtensions and delegates authentication to them.
 */
@Component
public class DelegatingPluginAuthenticationProvider implements AuthenticationProvider {

  private final Pf4jPluginBox pluginBox;

  public DelegatingPluginAuthenticationProvider(Pf4jPluginBox pluginBox) {
    this.pluginBox = pluginBox;
  }

  private List<AuthenticationProvider> getPluginProviders() {
    return pluginBox.getPlugins().stream()
        .filter(plugin -> ExtensionPoint.AUTH.equals(plugin.getType()))
        .map(plugin -> pluginBox.getInstance(plugin.getId(), AuthExtension.class).orElse(null))
        .filter(Objects::nonNull)
        .map(AuthExtension::getAuthenticationProvider)
        .collect(Collectors.toList());
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    AuthenticationException lastException = null;
    for (AuthenticationProvider provider : getPluginProviders()) {
      if (!provider.supports(authentication.getClass())) {
        continue;
      }
      try {
        Authentication result = provider.authenticate(authentication);
        if (result != null) {
          return result;
        }
      } catch (AuthenticationException e) {
        lastException = e;
      }
    }
    if (lastException != null) {
      throw lastException;
    }
    return null; // Let ProviderManager continue or fail
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return getPluginProviders().stream().anyMatch(provider -> provider.supports(authentication));
  }
}
