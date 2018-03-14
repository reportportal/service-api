package com.epam.ta.reportportal.auth.config;

import com.epam.ta.reportportal.auth.OAuthSuccessHandler;
import com.epam.ta.reportportal.auth.ReportPortalClient;
import com.epam.ta.reportportal.auth.UserRoleHierarchy;
import com.epam.ta.reportportal.auth.basic.BasicPasswordAuthenticationProvider;
import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.auth.integration.MutableClientRegistrationRepository;
import com.epam.ta.reportportal.auth.permissions.PermissionEvaluatorFactoryBean;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

public class SecurityConfiguration {

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
	@Order(4)
	public static class GlobalWebSecurityConfig extends WebSecurityConfigurerAdapter {

		public static final String SSO_LOGIN_PATH = "/sso/login";

		@Autowired
		private OAuthSuccessHandler successHandler;

		@Override
		protected final void configure(HttpSecurity http) throws Exception {
			//@formatter:off
        http
                .antMatcher("/**")
                .authorizeRequests()
                .antMatchers(SSO_LOGIN_PATH + "/**", "/epam/**", "/info", "/health", "/api-docs/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
					.csrf().disable()
				.formLogin().disable()
				.sessionManagement()
                	.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and().authenticationProvider(basicPasswordAuthProvider())
					.httpBasic()
				.and()
					.oauth2Login().clientRegistrationRepository(clientRegistrationRepository())
					.redirectionEndpoint().baseUri(SSO_LOGIN_PATH).and()
					.authorizationEndpoint().baseUri(SSO_LOGIN_PATH).and()
//					.loginPage(SSO_LOGIN_PATH + "/{registrationId}")
					.successHandler(successHandler);

		}

		@Bean
		protected UserDetailsService userDetailsService() {
			return new DatabaseUserDetailsService();
		}

		@Bean
		public AuthenticationProvider basicPasswordAuthProvider() {
			BasicPasswordAuthenticationProvider provider = new BasicPasswordAuthenticationProvider();
			provider.setUserDetailsService(userDetailsService());
			provider.setPasswordEncoder(passwordEncoder());
			return provider;
		}

		public PasswordEncoder passwordEncoder() {
			return new MD5PasswordEncoder();
		}

		@Override
		@Primary
		@Bean
		public AuthenticationManager authenticationManager() throws Exception {
			 return super.authenticationManager();
		}


		@Autowired
		DSLContext dslContext;

		@Bean
		public ClientRegistrationRepository clientRegistrationRepository(){
			return new MutableClientRegistrationRepository(dslContext);
		}

	}

	@Configuration
	@Order(3)
	@EnableAuthorizationServer
	public static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

		private final AuthenticationManager authenticationManager;

		@Autowired
		public AuthorizationServerConfiguration(AuthenticationManager authenticationManager) {
			this.authenticationManager = authenticationManager;
		}

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
			endpoints
					.tokenStore(tokenStore())
					.accessTokenConverter(accessTokenConverter())
					.authenticationManager(authenticationManager);
		}

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
			//@formatter:off
            clients.inMemory()
                    .withClient(ReportPortalClient.ui.name())
                    	.secret("{bcrypt}$2a$10$ka8W./nA2Uiqsd2uOzazdu2lMbipaMB6RJNInB1Y0NMKQzj7plsie")
                    	.authorizedGrantTypes("refresh_token", "password")
                    	.scopes("ui")
						.accessTokenValiditySeconds(500)
                    .and()
                    .withClient(ReportPortalClient.api.name())
                    	.secret("apiman")
						.authorizedGrantTypes("password")
                    	.scopes("api")
                    	.accessTokenValiditySeconds(-1)

                    .and()
                    .withClient(ReportPortalClient.internal.name())
                    	.secret("internal_man")
                    	.authorizedGrantTypes("client_credentials").authorities("ROLE_INTERNAL")
                    	.scopes("internal");

            //@formatter:on
		}

		@Override
		public void configure(AuthorizationServerSecurityConfigurer security) {

			//		security.tokenKeyAccess("hasAuthority('ROLE_INTERNAL')").checkTokenAccess("hasAuthority('ROLE_INTERNAL')");

		}

		@Bean
		public TokenStore tokenStore() {
			return new JwtTokenStore(accessTokenConverter());
		}

		@Bean
		public JwtAccessTokenConverter accessTokenConverter() {
			JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
			converter.setSigningKey("123");
			return converter;
		}

		@Bean
		@Primary
		public DefaultTokenServices tokenServices() {
			DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
			defaultTokenServices.setTokenStore(tokenStore());
			defaultTokenServices.setSupportRefreshToken(true);
			defaultTokenServices.setAuthenticationManager(authenticationManager);
			return defaultTokenServices;
		}

	}

	@Configuration
	@EnableResourceServer
	public static class ResourceServerAuthConfiguration extends ResourceServerConfigurerAdapter {
		@Override
		public void configure(HttpSecurity http) throws Exception {
			http
					.requestMatchers()
					.antMatchers("/sso/me/**", "/sso/internal/**", "/settings/**")
					.and()
					.authorizeRequests()
					.accessDecisionManager(webAccessDecisionManager())
					.antMatchers("/settings/**")
					.hasRole("ADMINISTRATOR")
					.antMatchers("/sso/internal/**")
					.hasRole("INTERNAL")
					.anyRequest()
					.authenticated()
					.and()
					.sessionManagement()
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		}

		@Bean
		public static RoleHierarchy userRoleHierarchy() {
			return new UserRoleHierarchy();
		}

		@Autowired
		private PermissionEvaluator permissionEvaluator;

		@Bean
		public static PermissionEvaluatorFactoryBean permissionEvaluatorFactoryBean() {
			return new PermissionEvaluatorFactoryBean();
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

	}

	public static class MD5PasswordEncoder implements PasswordEncoder {

		private HashFunction hasher = Hashing.md5();

		@Override
		public String encode(CharSequence rawPassword) {
			return hasher.newHasher().putString(rawPassword, Charsets.UTF_8).hash().toString();
		}

		@Override
		public boolean matches(CharSequence rawPassword, String encodedPassword) {
			if (isNullOrEmpty(encodedPassword)) {
				return false;
			}
			return encodedPassword.equals(hasher.newHasher().putString(rawPassword, Charsets.UTF_8).hash().toString());
		}

	}

	//	static class ReportPortalAuthorityExtractor extends FixedAuthoritiesExtractor {
	//		@Override
	//		public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
	//			List<GrantedAuthority> userRoles = super.extractAuthorities(map);
	//			Optional.ofNullable(map.get("projects"))
	//					.map(p -> ((Map<String, String>) p).entrySet()
	//							.stream()
	//							.map(e -> new ProjectAuthority(e.getKey(), e.getValue()))
	//							.collect(Collectors.toList()))
	//					.ifPresent(userRoles::addAll);
	//			return userRoles;
	//		}
	//	}
}
