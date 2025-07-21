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

package com.epam.ta.reportportal.core.configs.security;

import static com.epam.ta.reportportal.core.configs.security.UserResolverType.EXTERNAL;

import com.epam.ta.reportportal.auth.userdetails.DefaultUserDetailsService;
import com.epam.ta.reportportal.auth.userdetails.ExternalUserDetailsService;
import com.epam.ta.reportportal.core.configs.security.converters.ExternalJwtConverter;
import com.epam.ta.reportportal.core.configs.security.converters.ReportPortalJwtConverter;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;

/**
 * Configuration for handling multiple identity providers with JWT authentication. This configuration allows for
 * different JWT issuers to be configured and used for authentication based on the issuer URI.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class MultiIdentityProviderConfig {

  private static final String SETTING_KEY_SECRET = "secret.key";

  private final UserDetailsService userDetailsService;
  private final ExternalUserDetailsService externalUserDetailsService;
  private final ServerSettingsRepository serverSettingsRepository;

  /**
   * Constructs a MultiIdentityProviderConfig with the necessary services.
   */
  @Autowired
  public MultiIdentityProviderConfig(
      DefaultUserDetailsService userDetailsService,
      ExternalUserDetailsService externalUserDetailsService,
      ServerSettingsRepository serverSettingsRepository
  ) {
    this.userDetailsService = userDetailsService;
    this.externalUserDetailsService = externalUserDetailsService;
    this.serverSettingsRepository = serverSettingsRepository;
  }

  /**
   * Validates the configuration after initialization. Logs a warning if no identity providers are configured.
   */
  @PostConstruct
  public void validate() {
    if (identityProviderConfig().getProvider().isEmpty()) {
      log.warn("No identity providers configured");
    }
  }

  /**
   * Configuration properties for identity providers. This class holds the configuration for each JWT issuer, including
   * the issuer URI, signing key, algorithm, and user details service.
   */
  @ConfigurationProperties(prefix = "rp.oauth2")
  @Data
  public static class IdentityProviderConfig {

    private Map<String, JwtIssuerConfig> provider = new HashMap<>();
  }

  /**
   * Bean for creating the IdentityProviderConfig.
   */
  @Bean
  public IdentityProviderConfig identityProviderConfig() {
    return new IdentityProviderConfig();
  }

  /**
   * Bean for resolving JWT issuer authentication managers. This resolver maps issuer URIs to their respective
   * authentication managers.
   *
   * @return JwtIssuerAuthenticationManagerResolver
   */
  @Bean
  public JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver() {
    Map<String, AuthenticationManager> jwtManagers = new HashMap<>();

    var config = identityProviderConfig();

    config.getProvider().forEach((name, issuerConfig) -> {
      if (issuerConfig.getIssuerUri() != null && !issuerConfig.getIssuerUri().trim().isEmpty()) {
        jwtManagers.put(issuerConfig.getIssuerUri(), createProviderAuthenticationManager(name, issuerConfig));
        log.info("Added JWT issuer: {} with URI: {}", name, issuerConfig.getIssuerUri());
      }
    });

    return new JwtIssuerAuthenticationManagerResolver(jwtManagers::get);
  }

  private AuthenticationManager createProviderAuthenticationManager(String name, JwtIssuerConfig config) {
    var decoder = createJwtDecoder(name, config);
    var provider = new JwtAuthenticationProvider(decoder);
    provider.setJwtAuthenticationConverter(createJwtConverter(name, config));

    return new ProviderManager(provider);
  }

  private JwtDecoder createJwtDecoder(String name, JwtIssuerConfig config) {
    if (name.contentEquals("internal")) {
      var algorithm = config.getAlgorithm();
      var key = StringUtils.isNotEmpty(config.getSecretKey())
          ? config.getSecretKey()
          : generateDefaultKey();
      var secretKey = convertToSecretKey(key, algorithm);
      return NimbusJwtDecoder.withSecretKey(secretKey)
          .macAlgorithm(MacAlgorithm.from(algorithm))
          .build();
    }

    if (StringUtils.isNotEmpty(config.getJwkSetUri())) {
      return NimbusJwtDecoder.withJwkSetUri(config.getJwkSetUri())
          .build();
    }

    if (StringUtils.isNotEmpty(config.getSecretKey())) {
      var algorithm = config.getAlgorithm();
      var secretKey = convertToSecretKey(config.getSecretKey(), algorithm);
      return NimbusJwtDecoder.withSecretKey(secretKey)
          .macAlgorithm(MacAlgorithm.from(algorithm))
          .build();
    }

    throw new IllegalArgumentException("Either jwkSetUri or signingKey must be provided");
  }

  private Converter<Jwt, AbstractAuthenticationToken> createJwtConverter(String name, JwtIssuerConfig config) {
    if (name.equals("rp")) {
      return new ReportPortalJwtConverter(userDetailsService);
    }

    var detailsService = config.getUserResolver() == EXTERNAL
        ? externalUserDetailsService
        : userDetailsService;

    return new ExternalJwtConverter(detailsService, config);
  }

  private SecretKeySpec convertToSecretKey(String key, String algorithm) {
    return new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), getJavaAlgorithmName(algorithm));
  }

  private String generateDefaultKey() {
    return serverSettingsRepository.findByKey(SETTING_KEY_SECRET)
        .map(ServerSettings::getValue)
        .orElseGet(serverSettingsRepository::generateSecret);
  }

  private String getJavaAlgorithmName(String algorithm) {
    return switch (algorithm) {
      case "HS384" -> "HmacSHA384";
      case "HS512" -> "HmacSHA512";
      default -> "HmacSHA256";
    };
  }
}
