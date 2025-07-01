package com.epam.ta.reportportal.core.configs.security;

import com.epam.ta.reportportal.core.configs.security.converters.AzureJwtConverter;
import com.epam.ta.reportportal.core.configs.security.converters.GoogleJwtConverter;
import com.epam.ta.reportportal.core.configs.security.converters.InternalJwtConverter;
import com.epam.ta.reportportal.core.configs.security.converters.ReportPortalJwtConverter;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;

@Log4j2
@Configuration
@EnableWebSecurity
public class MultiIdentityProviderConfig {

  private static final String SETTING_KEY_SECRET = "secret.key";

  private final UserDetailsService userDetailsService;
  private final ServerSettingsRepository serverSettingsRepository;

  @Autowired
  public MultiIdentityProviderConfig(
      UserDetailsService userDetailsService,
      ServerSettingsRepository serverSettingsRepository
  ) {
    this.userDetailsService = userDetailsService;
    this.serverSettingsRepository = serverSettingsRepository;
  }

  @ConfigurationProperties(prefix = "oauth2.resource-server")
  @Data
  public static class IdentityProviderConfig {

    private Map<String, JwtIssuerConfig> providers = new HashMap<>();
  }

  @Data
  public static class JwtIssuerConfig {

    private String issuerUri;
    private String jwkSetUri;
    private String signingKey;
    private String algorithm = "HS256";
    private String usernameClaim = "sub";
    private String authoritiesClaim = "authorities";
  }

  @PostConstruct
  public void validate() {
    if (identityProviderConfig().getProviders().isEmpty()) {
      log.warn("No identity providers configured");
    }
  }

  @Bean
  public IdentityProviderConfig identityProviderConfig() {
    return new IdentityProviderConfig();
  }

  @Bean
  public AuthenticationManagerResolver<HttpServletRequest> customAuthenticationManagerResolver(
      ApiKeyAuthenticationProvider apiKeyAuthenticationProvider
  ) {

    Map<String, AuthenticationManager> jwtManagers = new HashMap<>();
    var config = identityProviderConfig();
    config.getProviders().forEach((name, issuerConfig) -> {
      if (issuerConfig.getIssuerUri() != null && !issuerConfig.getIssuerUri().trim().isEmpty()) {
        jwtManagers.put(issuerConfig.getIssuerUri(), createProviderAuthenticationManager(name, issuerConfig));
      }
    });

    var jwtResolver = new JwtIssuerAuthenticationManagerResolver(jwtManagers::get);
    var apiKeyManager = new ProviderManager(apiKeyAuthenticationProvider);

    return request -> {
      if (isJWT(request)) {
        return jwtResolver.resolve(request);
      } else {
        return apiKeyManager;
      }
    };
  }

  private boolean isJWT(HttpServletRequest request) {
    try {
      var token = getBearerValue(request);
      var parts = token.split("\\.");
      if (parts.length != 3) {
        return false;
      }
      try {
        java.util.Base64.getUrlDecoder().decode(parts[1]);
        return true;
      } catch (Exception e) {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

  private String getBearerValue(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
      return authHeader.substring(7);
    }
    throw new IllegalArgumentException("No Bearer token found");
  }

  private AuthenticationManager createProviderAuthenticationManager(String name, JwtIssuerConfig config) {
    JwtDecoder decoder = createJwtDecoder(name, config);

    JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
    provider.setJwtAuthenticationConverter(createJwtConverter(name));

    return new ProviderManager(provider);
  }

  private JwtDecoder createJwtDecoder(String name, JwtIssuerConfig config) {
    if (config.getJwkSetUri() != null && !config.getJwkSetUri().trim().isEmpty()) {
      return NimbusJwtDecoder.withJwkSetUri(config.getJwkSetUri()).build();
    }

    if (config.getSigningKey() != null && !config.getSigningKey().trim().isEmpty()) {
      return NimbusJwtDecoder.withSecretKey(getSecretKey(config.getSigningKey(), config.getAlgorithm()))
          .macAlgorithm(MacAlgorithm.from(config.getAlgorithm()))
          .build();
    }

    if (Objects.equals(name, "rp")) {
      return NimbusJwtDecoder.withSecretKey(getDefaultSecretKey(config.getAlgorithm()))
          .macAlgorithm(MacAlgorithm.from(config.getAlgorithm()))
          .build();
    }

    throw new IllegalArgumentException("Either jwkSetUri or signingKey must be provided");
  }

  private Converter<Jwt, AbstractAuthenticationToken> createJwtConverter(String name) {
    return switch (name) {
      case "google" -> new GoogleJwtConverter(userDetailsService);
      case "azure" -> new AzureJwtConverter(userDetailsService);
      case "internal" -> new InternalJwtConverter(userDetailsService);
      default -> new ReportPortalJwtConverter(userDetailsService);
    };
  }

  private SecretKeySpec getSecretKey(String key, String algorithm) {
    String javaAlgorithm = getJavaAlgorithmName(algorithm);
    return new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), javaAlgorithm);
  }

  private SecretKeySpec getDefaultSecretKey(String algorithm) {
    var secret = serverSettingsRepository.findByKey(SETTING_KEY_SECRET)
        .map(ServerSettings::getValue)
        .orElseGet(serverSettingsRepository::generateSecret);

    String javaAlgorithm = getJavaAlgorithmName(algorithm);
    return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), javaAlgorithm);
  }

  private String getJavaAlgorithmName(String algorithm) {
    return switch (algorithm) {
      case "HS256" -> "HmacSHA256";
      case "HS384" -> "HmacSHA384";
      case "HS512" -> "HmacSHA512";
      default -> "HmacSHA256";
    };
  }
}
