/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-authorization
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.auth;

//import org.springframework.security.oauth2.provider.*;
//import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Facade for {@link org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices} to simplify work with them
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Service
public class TokenServicesFacade {

	private final DefaultTokenServices tokenServices;
	private final OAuth2RequestFactory oAuth2RequestFactory;
	private final ClientDetailsService clientDetailsService;

	@Autowired
	public TokenServicesFacade(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService) {
		this.tokenServices = (DefaultTokenServices) tokenServices;
		this.clientDetailsService = clientDetailsService;
		this.oAuth2RequestFactory = new DefaultOAuth2RequestFactory(clientDetailsService);
	}

	public Stream<OAuth2AccessToken> getTokens(String username, ReportPortalClient client) {
		return Stream.empty();
	}

	public OAuth2AccessToken createToken(ReportPortalClient client, String username, Authentication userAuthentication) {
		return createToken(client, username, userAuthentication, Collections.emptyMap());
	}

	public OAuth2AccessToken createToken(ReportPortalClient client, String username, Authentication userAuthentication,
			Map<String, Serializable> extensionParams) {
		//@formatter:off
		ClientDetails clientDetails = clientDetailsService.loadClientByClientId(client.name());
		OAuth2Request oAuth2Request = oAuth2RequestFactory.createOAuth2Request(clientDetails, oAuth2RequestFactory.createTokenRequest(
				ImmutableMap.<String, String>builder()
						.put("client_id", client.name())
						.put("username", username)
						.put("grant", "password")
						.build(), clientDetails));
		oAuth2Request.getExtensions().putAll(extensionParams);
		//@formatter:on
		return tokenServices.createAccessToken(new OAuth2Authentication(oAuth2Request, userAuthentication));
	}

	public OAuth2AccessToken getAccessToken(Authentication userAuthentication) {
		return tokenServices.getAccessToken((OAuth2Authentication) userAuthentication);
	}
}
