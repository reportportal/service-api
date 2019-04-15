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
import com.epam.ta.reportportal.auth.permissions.PermissionEvaluatorFactoryBean;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

		@Autowired
		private PermissionEvaluator permissionEvaluator;

		@Autowired
		@Value("${rp.jwt.signing-key}")
		private String signingKey;

		@Bean
		public static PermissionEvaluatorFactoryBean permissionEvaluatorFactoryBean() {
			return new PermissionEvaluatorFactoryBean();
		}

		@Bean
		public TokenStore tokenStore() {
			return new JwtTokenStore(accessTokenConverter());
		}

		@Bean
		public JwtAccessTokenConverter accessTokenConverter() {
			JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
			converter.setSigningKey(signingKey);

			DefaultAccessTokenConverter converter1 = new DefaultAccessTokenConverter();
			converter1.setUserTokenConverter(new ReportPortalAuthenticationConverter());
			converter.setAccessTokenConverter(converter1);

			return converter;
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

		@Bean
		public static RoleHierarchy userRoleHierarchy() {
			return new UserRoleHierarchy();
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

							"/documentation.html"
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

	static class ReportPortalAuthenticationConverter extends DefaultUserAuthenticationConverter {
		@Override
		public Map<String, ?> convertUserAuthentication(Authentication authentication) {
			@SuppressWarnings("unchecked")
			Map<String, Object> claims = (Map<String, Object>) super.convertUserAuthentication(authentication);
			ReportPortalUser principal = (ReportPortalUser) authentication.getPrincipal();
			claims.put("userId", principal.getUserId());
			claims.put("userRole", principal.getUserRole());
			claims.put("projects", principal.getProjectDetails());
			claims.put("email", principal.getEmail());
			return claims;
		}

		@Override
		public Authentication extractAuthentication(Map<String, ?> map) {
			Authentication auth = super.extractAuthentication(map);
			if (null != auth) {
				UsernamePasswordAuthenticationToken user = ((UsernamePasswordAuthenticationToken) auth);
				Collection<GrantedAuthority> authorities = user.getAuthorities();

				Long userId = map.containsKey("userId") ? parseId(map.get("userId")) : null;
				UserRole userRole = map.containsKey("userRole") ? UserRole.valueOf(map.get("userRole").toString()) : null;
				String email = map.containsKey("email") ? String.valueOf(map.get("email").toString()) : null;

				Map<String, Map> projects = map.containsKey("projects") ? (Map) map.get("projects") : Collections.emptyMap();

				Map<String, ReportPortalUser.ProjectDetails> collect = projects.entrySet()
						.stream()
						.collect(Collectors.toMap(Map.Entry::getKey,
								e -> new ReportPortalUser.ProjectDetails(parseId(e.getValue().get("projectId")),
										(String) e.getValue().get("projectName"),
										ProjectRole.valueOf((String) e.getValue().get("projectRole"))
								)
						));

				return new UsernamePasswordAuthenticationToken(new ReportPortalUser(user.getName(),
						"N/A",
						authorities,
						userId,
						userRole,
						collect,
						email
				), user.getCredentials(), authorities);
			}

			return null;

		}

		private Long parseId(Object id) {
			if (id instanceof Integer) {
				return Long.valueOf((Integer) id);
			}
			return (Long) id;
		}
	}
}



