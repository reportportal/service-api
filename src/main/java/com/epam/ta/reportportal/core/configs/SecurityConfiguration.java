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

import com.epam.ta.reportportal.auth.CombinedTokenStore;
import com.epam.ta.reportportal.auth.UserRoleHierarchy;
import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.auth.permissions.PermissionEvaluatorFactoryBean;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Spring's Security Configuration
 *
 * @author Andrei Varabyeu
 */
@Configuration
class SecurityConfiguration {

	@Bean
	public PermissionEvaluatorFactoryBean permissionEvaluator() {
		return new PermissionEvaluatorFactoryBean();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Configuration
	@EnableGlobalMethodSecurity(proxyTargetClass = true, prePostEnabled = true)
	public static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

		@Autowired
		private RoleHierarchy roleHierarchy;

		@Autowired
		private PermissionEvaluator permissionEvaluator;

		@Override
		protected MethodSecurityExpressionHandler createExpressionHandler() {
			DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
			handler.setRoleHierarchy(roleHierarchy);
			handler.setPermissionEvaluator(permissionEvaluator);
			return handler;
		}

	}

	@Configuration
	@EnableResourceServer
	public static class SecurityServerConfiguration extends ResourceServerConfigurerAdapter {

		private static final String SECRET_KEY = "secret.key";

		@Value("${rp.jwt.signing-key}")
		private String signingKey;

		@Autowired
		private PermissionEvaluator permissionEvaluator;

		@Autowired
		private DatabaseUserDetailsService userDetailsService;

		@Autowired
		private ServerSettingsRepository serverSettingsRepository;

		@Bean
		public static PermissionEvaluatorFactoryBean permissionEvaluatorFactoryBean() {
			return new PermissionEvaluatorFactoryBean();
		}

		@Bean
		public static RoleHierarchy userRoleHierarchy() {
			return new UserRoleHierarchy();
		}

		@Bean
		public TokenStore tokenStore() {
			return new CombinedTokenStore(accessTokenConverter());
		}

		@Bean
		@Profile("!unittest")
		public JwtAccessTokenConverter accessTokenConverter() {
			JwtAccessTokenConverter jwtConverter = new JwtAccessTokenConverter();
			jwtConverter.setSigningKey(getSecret());

			DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
			DefaultUserAuthenticationConverter defaultUserAuthenticationConverter = new DefaultUserAuthenticationConverter();
			defaultUserAuthenticationConverter.setUserDetailsService(userDetailsService);
			accessTokenConverter.setUserTokenConverter(defaultUserAuthenticationConverter);

			jwtConverter.setAccessTokenConverter(accessTokenConverter);

			return jwtConverter;
		}

		private String getSecret() {
			if (!StringUtils.isEmpty(signingKey)) {
				return signingKey;
			}
			Optional<ServerSettings> secretKey = serverSettingsRepository.findByKey(SECRET_KEY);
			return secretKey.isPresent() ? secretKey.get().getValue() : serverSettingsRepository.generateSecret();
		}

		@Bean
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
		}

		@Override
		public void configure(HttpSecurity http) throws Exception {
			http.authorizeRequests()
					.accessDecisionManager(webAccessDecisionManager())
					.antMatchers("/**/user/registration/info*",
							"/**/user/registration**",
							"/**/user/password/reset/*",
							"/**/user/password/reset**",
							"/**/user/password/restore**",
							"/documentation.html",
							"/health",
							"/info"
					)
					.permitAll()
					/* set of special endpoints for another microservices from RP ecosystem */
					.antMatchers("/api-internal/**")
					.hasRole("COMPONENT")
					.antMatchers("/v2/**", "/swagger-resources", "/certificate/**", "/api/**", "/**")
					.hasRole("USER")
					.anyRequest()
					.authenticated()
					.and()
					.csrf()
					.disable();
		}

	}
}



