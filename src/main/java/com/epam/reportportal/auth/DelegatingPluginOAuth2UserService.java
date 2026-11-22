package com.epam.reportportal.auth;

import com.epam.reportportal.auth.store.OAuthRegistrationResourceRepository;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.extension.AuthExtension;
import com.epam.reportportal.extension.common.ExtensionPoint;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dynamically resolves the OAuth2 user service from loaded auth plugins on each request.
 * Mirrors the pattern of {@link DelegatingPluginAuthenticationProvider}.
 */
@Component
public class DelegatingPluginOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final Pf4jPluginBox pluginBox;
  private final OAuthRegistrationResourceRepository clientRegistrationRepository;

  public DelegatingPluginOAuth2UserService(Pf4jPluginBox pluginBox,
      OAuthRegistrationResourceRepository clientRegistrationRepository) {
    this.pluginBox = pluginBox;
    this.clientRegistrationRepository = clientRegistrationRepository;
  }

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    OAuth2User result = pluginBox.getPlugins().stream()
        .filter(plugin -> ExtensionPoint.AUTH.equals(plugin.getType()))
        .map(plugin -> pluginBox.getInstance(plugin.getId(), AuthExtension.class).orElse(null))
        .filter(Objects::nonNull)
        .map(AuthExtension::getOAuthProvider)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(provider -> provider.getName().equals(registrationId))
        .findFirst()
        .map(provider -> provider.getUserService(
            clientRegistrationRepository.findOAuthRegistrationResourceById(registrationId)))
        .map(svc -> svc.loadUser(userRequest))
        .orElse(null);

    if (result == null) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("invalid_user_info_response", "No OAuth provider plugin handles registration " + registrationId,
              null));
    }
    return result;
  }
}
