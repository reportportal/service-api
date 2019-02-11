package com.epam.ta.reportportal.auth;

import com.epam.ta.reportportal.entity.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class OAuthHelper {

	@Autowired
	private AuthorizationServerTokenServices tokenService;

	private String defaultToken;

	private String superadminToken;

	public String getDefaultToken() {
		return defaultToken == null ? defaultToken = createAccessToken("default", "1q2w3e", UserRole.USER).getValue() : defaultToken;
	}

	public String getSuperadminToken() {
		return superadminToken == null ?
				superadminToken = createAccessToken("superadmin", "erebus", UserRole.ADMINISTRATOR).getValue() :
				superadminToken;
	}

	private OAuth2AccessToken createAccessToken(String username, String password, UserRole... roles) {
		Collection<GrantedAuthority> authorities = Arrays.stream(roles)
				.map(it -> new SimpleGrantedAuthority(it.getAuthority()))
				.collect(Collectors.toList());

		Set<String> scopes = Collections.singleton("ui");

		Map<String, String> requestParameters = new HashMap<>();
		requestParameters.put("password", password);
		requestParameters.put("grand_type", "password");
		requestParameters.put("username", username);

		OAuth2Request oAuth2Request = new OAuth2Request(
				requestParameters,
				"ui",
				authorities,
				true,
				scopes,
				Collections.emptySet(),
				null,
				Collections.emptySet(),
				Collections.emptyMap()
		);

		User userPrincipal = new User(username, password, true, true, true, true, authorities);
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
		OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);
		return tokenService.createAccessToken(auth);
	}
}