/*
 * Copyright 2019 EPAM Systems
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
package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.auth.UserRoleHierarchy;
import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.auth.permissions.PermissionEvaluatorFactoryBean;
import com.epam.ta.reportportal.core.configs.filter.ApiKeyFilter;
import com.epam.ta.reportportal.core.configs.filter.JwtFilter;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

/**
 * Spring's Security Configuration
 *
 * @author Andrei Varabyeu
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(proxyTargetClass = true)
class SecurityConfiguration {

  @Value("${rp.jwt.signing-key}")
  private String signingKey;

  @Autowired
  private PermissionEvaluator permissionEvaluator;

  @Autowired
  private DatabaseUserDetailsService userDetailsService;

  @Autowired
  private ServerSettingsRepository serverSettingsRepository;

  @Autowired
  private RoleHierarchy roleHierarchy;

  @Autowired
  JwtFilter jwtFilter;

  @Autowired
  ApiKeyFilter apiKeyFilter;

  private static final String SECRET_KEY = "secret.key";

  @Bean
  @Profile("!unittest")
  public JwtAuthenticationConverter accessTokenConverter() {

    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthoritiesConverter());
    jwtConverter.setPrincipalClaimName("sub");
/*
    JwtBearerTokenAuthenticationConverter jwtConverter = new JwtBearerTokenAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthoritiesConverter());

    DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
    DefaultUserAuthenticationConverter defaultUserAuthenticationConverter = new DefaultUserAuthenticationConverter();
    defaultUserAuthenticationConverter.setUserDetailsService(userDetailsService);
    accessTokenConverter.setUserTokenConverter(defaultUserAuthenticationConverter);

    jwtConverter.setAccessTokenConverter(accessTokenConverter);*/

    return jwtConverter;
  }

  @Bean
  public SecurityFilterChain web(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/**/user**/registration/info*",
                "/**/user**/registration**",
                "/**/user**/password/reset/*",
                "/**/user**/password/reset**",
                "/**/user**/password/restore**",
                "/**/plugin/public/**",
                "/documentation.html",
                "/health",
                "/info"
            )
            .permitAll()
            /* set of special endpoints for another microservices from RP ecosystem */
            .requestMatchers("/api-internal/**")
            .hasRole("COMPONENT")
            .requestMatchers("/v2/**", "/swagger-resources", "/certificate/**", "/api/**", "/**")
            .hasRole("USER")
            .anyRequest()
            .authenticated())
/*        .oauth2ResourceServer(resourceServer ->
            resourceServer.jwt(
                jwt -> jwt.jwtAuthenticationConverter(accessTokenConverter())))*/
        .userDetailsService(userDetailsService)
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(apiKeyFilter, RememberMeAuthenticationFilter.class)
        .csrf(AbstractHttpConfigurer::disable);

    return http.build();
  }

/*  @Bean
  public TokenStore tokenStore() {
    return new CombinedTokenStore(accessTokenConverter());
  }*/

  @Bean
  public PermissionEvaluatorFactoryBean permissionEvaluator() {
    return new PermissionEvaluatorFactoryBean();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public static RoleHierarchy userRoleHierarchy() {
    return new UserRoleHierarchy();
  }

  @Bean
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(this.getSecret()).build();
  }

  @Bean
  public MethodSecurityExpressionHandler createExpressionHandler() {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    handler.setPermissionEvaluator(permissionEvaluator);
    return handler;
  }

/*  @Configuration
  @EnableResourceServer
  public static class SecurityServerConfiguration extends ResourceServerConfigurerAdapter {


    @Bean
    public static PermissionEvaluatorFactoryBean permissionEvaluatorFactoryBean() {
      return new PermissionEvaluatorFactoryBean();
    }

    @Bean
    public static RoleHierarchy userRoleHierarchy() {
      return new UserRoleHierarchy();
    }*/


/*    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
      DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
      defaultTokenServices.setTokenStore(tokenStore());
      defaultTokenServices.setSupportRefreshToken(true);
      defaultTokenServices.setTokenEnhancer(accessTokenConverter());
      return defaultTokenServices;
    }

    private DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
      OAuth2WebSecurityExpressionHandler handler = new OAuth2WebSecurityExpressionHandler();
      handler.setRoleHierarchy(userRoleHierarchy());
      handler.setPermissionEvaluator(permissionEvaluator);
      return handler;
    }

    private AccessDecisionManager webAccessDecisionManager() {
      List<AccessDecisionVoter<?>> accessDecisionVoters = Lists.newArrayList();
      accessDecisionVoters.add(new AuthenticatedVoter());
      WebExpressionVoter webVoter = new WebExpressionVoter();
      webVoter.setExpressionHandler(webSecurityExpressionHandler());
      accessDecisionVoters.add(webVoter);

      return new AffirmativeBased(accessDecisionVoters);
    }*/


  private SecretKey getSecret() {
    String secret = Optional.ofNullable(signingKey)
        .filter(StringUtils::isNotEmpty)
        .orElseGet(() -> serverSettingsRepository.findByKey(SECRET_KEY)
            .map(ServerSettings::getValue)
            .orElseGet(() -> serverSettingsRepository.generateSecret()));

    return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), 0, secret.length(), "HmacSha256");
  }
}



