/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.auth.config;

import com.epam.reportportal.auth.DelegatingPluginAuthenticationProvider;
import com.epam.reportportal.auth.DelegatingPluginOAuth2UserService;
import com.epam.reportportal.auth.OAuthSuccessHandler;
import com.epam.reportportal.auth.basic.BasicPasswordAuthenticationProvider;
import com.epam.reportportal.auth.config.utils.JwtReportPortalUserConverter;
import com.epam.reportportal.auth.event.UiAuthenticationFailureEventHandler;
import com.epam.reportportal.auth.store.MutableClientRegistrationRepository;
import com.epam.reportportal.base.core.auth.TokenBlacklistService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

/**
 * Spring Security configuration that sets up JWT token support, SSO (OAuth2/SAML) login, and the security filter
 * chains, including the permit-all chain backing the username/password login endpoint.
 *
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthorizationServerConfig {

  private static final String SECRET_KEY = "secret.key";
  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private static final String OAUTH_REQUEST_KEY_CONTEXT = "rp-oauth2-authorization-request-v1";
  private final ServerSettingsRepository serverSettingsRepository;
  private final MutableClientRegistrationRepository clientRegistrationRepository;
  private final AuthenticationFailureHandler authenticationFailureHandler;
  private final PasswordEncoder passwordEncoder;
  private final UserDetailsService userDetailsService;
  private final TokenBlacklistService tokenBlacklistService;
  private final DelegatingPluginAuthenticationProvider delegatingPluginAuthenticationProvider;
  private final DelegatingPluginOAuth2UserService delegatingPluginOAuth2UserService;
  @Value("${rp.jwt.signing-key}")
  private String signingKey;
  @Value("${rp.jwt.token.validity-period}")
  private Integer tokenValidity;
  @Value("${rp.jwt.issuer}")
  private String jwtIssuer;
  @Value("${rp.auth.cookie.secure.enforce-https:true}")
  private boolean cookieSecureEnforceHttps;

  @Bean
  public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository(JsonMapper objectMapper) {
    return new HttpCookieOAuth2AuthorizationRequestRepository(objectMapper, cookieSecureEnforceHttps,
        deriveCookieKey(getSecret()));
  }

  /**
   * Derives a feature-specific key so the master secret is never used directly for this cookie.
   */
  private static byte[] deriveCookieKey(String masterSecret) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(masterSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
      return mac.doFinal(OAUTH_REQUEST_KEY_CONTEXT.getBytes(StandardCharsets.UTF_8));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Cannot derive OAuth2 authorization request cookie key", e);
    }
  }

  @Bean
  @Profile("!unittest")
  public JwtEncoder jwtEncoder() {
    SecretKey key = new SecretKeySpec(getSecret().getBytes(),
        HMAC_ALGORITHM);
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }

  @Bean
  @Profile("!unittest")
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(getSecret().getBytes(), "HmacSHA256")).build();
  }

  @Bean
  @Order(1)
  SecurityFilterChain loginSecurityFilterChain(HttpSecurity http) {
    http
        .securityMatcher("/sso/oauth/token")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  @Bean
  public AuthenticationProvider basicPasswordAuthProvider(ApplicationEventPublisher eventPublisher,
      UiAuthenticationFailureEventHandler failureEventHandler, Provider<HttpServletRequest> request) {
    return new BasicPasswordAuthenticationProvider(userDetailsService, passwordEncoder, eventPublisher,
        failureEventHandler, request);
  }

  /**
   * Authentication manager backing the username/password login endpoint, delegating to the local DB-backed provider and
   * to plugin-provided providers (e.g. LDAP, Active Directory).
   */
  @Bean
  public AuthenticationManager loginAuthenticationManager(AuthenticationProvider basicPasswordAuthProvider,
      ApplicationEventPublisher eventPublisher) {
    ProviderManager manager = new ProviderManager(
        List.of(basicPasswordAuthProvider, delegatingPluginAuthenticationProvider));
    // ProviderManager publishes exactly one failure event per rejected login (from the final exception), which feeds
    // UiAuthenticationFailureEventHandler. The default publisher is a no-op.
    manager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(eventPublisher));
    return manager;
  }

  private String getSecret() {
    if (StringUtils.hasText(signingKey)) {
      return signingKey;
    }

    return serverSettingsRepository.findByKey(SECRET_KEY)
        .map(ServerSettings::getValue)
        .orElseGet(serverSettingsRepository::generateSecret);
  }

  @Bean
  @Order(10)
  public SecurityFilterChain globalWebSecurityFilterChain(
      HttpSecurity http,
      OAuth2AuthorizationRequestResolver authorizationRequestResolver,
      OAuthSuccessHandler successHandler,
      HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository) {
    http
        .securityMatcher("/**")
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/oauth/login/**",
                "/saml2/**",
                "/login/**")
            .permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2ResourceServerCustomizer())
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo ->
                userInfo.userService(delegatingPluginOAuth2UserService))
            .clientRegistrationRepository(clientRegistrationRepository)
            .authorizationEndpoint(authorization -> authorization
                .baseUri("/oauth/login")
                .authorizationRequestResolver(authorizationRequestResolver)
                .authorizationRequestRepository(cookieAuthorizationRequestRepository))
            .redirectionEndpoint(redirection -> redirection.baseUri("/sso/login/*"))
            .successHandler(successHandler)
            .failureHandler(authenticationFailureHandler))
        .oauth2Client(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  @Order(6)
  public SecurityFilterChain ssoSecurityFilterChain(HttpSecurity http) {
    http
        .securityMatcher("/sso/me/**")
        .authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2ResourceServerCustomizer())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  @Bean
  public OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
    return new DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository,
        "/oauth/login");
  }

  @Bean
  public OAuth2AuthorizedClientManager authorizedClientManager(
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientRepository authorizedClientRepository) {
    return new DefaultOAuth2AuthorizedClientManager(
        clientRegistrationRepository,
        authorizedClientRepository);
  }

  private Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>> oauth2ResourceServerCustomizer() {
    return oauth2 -> oauth2
        .jwt(jwt -> jwt
            .decoder(jwtDecoder())
            .jwtAuthenticationConverter(new JwtReportPortalUserConverter(userDetailsService, tokenBlacklistService)));
  }

}
