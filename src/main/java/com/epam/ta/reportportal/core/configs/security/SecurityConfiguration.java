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

import com.epam.ta.reportportal.auth.CustomAuthenticationEntryPoint;
import com.epam.ta.reportportal.auth.UserRoleHierarchy;
import com.epam.ta.reportportal.auth.userdetails.DefaultUserDetailsService;
import com.epam.ta.reportportal.auth.permissions.PermissionEvaluatorFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring's Security Configuration
 *
 * @author Andrei Varabyeu
 * @author Reingold Shekhtel
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(proxyTargetClass = true)
class SecurityConfiguration {

  private final PermissionEvaluator permissionEvaluator;
  private final DefaultUserDetailsService userDetailsService;
  private final RoleHierarchy roleHierarchy;
  private final CustomAuthenticationManagerResolver customAuthenticationManagerResolver;

  @Autowired
  public SecurityConfiguration(
      PermissionEvaluator permissionEvaluator,
      DefaultUserDetailsService userDetailsService,
      RoleHierarchy roleHierarchy,
      CustomAuthenticationManagerResolver customAuthenticationManagerResolver
  ) {
    this.permissionEvaluator = permissionEvaluator;
    this.userDetailsService = userDetailsService;
    this.roleHierarchy = roleHierarchy;
    this.customAuthenticationManagerResolver = customAuthenticationManagerResolver;
  }

  @Bean
  public SecurityFilterChain web(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/**/user**/registration/info*",
                "/**/invitations/**",
                "/**/user**/password/reset/*",
                "/**/user**/password/reset**",
                "/**/user**/password/restore**",
                "/**/plugin/public/**",
                "/documentation.html",
                "/health",
                "/info",
                "/api-docs"
            )
            .permitAll()
            /* set of special endpoints for another microservices from RP ecosystem */
            .requestMatchers("/api-internal/**")
            .hasRole("COMPONENT")
            .requestMatchers("/v2/**", "/swagger-resources", "/certificate/**", "/api/**", "/**")
            .hasRole("USER")
            .anyRequest()
            .authenticated())
        .oauth2ResourceServer(resourceServer -> resourceServer
            .authenticationManagerResolver(customAuthenticationManagerResolver)
            .authenticationEntryPoint(authenticationEntryPoint()))
        .userDetailsService(userDetailsService)
        .csrf(AbstractHttpConfigurer::disable);

    return http.build();
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    return new CustomAuthenticationEntryPoint();
  }

  @Bean
  public static PermissionEvaluatorFactoryBean permissionEvaluatorFactoryBean() {
    return new PermissionEvaluatorFactoryBean();
  }

  @Bean
  public static RoleHierarchy userRoleHierarchy() {
    return new UserRoleHierarchy();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public MethodSecurityExpressionHandler createExpressionHandler() {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    handler.setPermissionEvaluator(permissionEvaluator);
    return handler;
  }
}



