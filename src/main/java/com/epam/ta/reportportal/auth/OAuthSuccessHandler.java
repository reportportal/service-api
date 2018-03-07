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

import com.epam.ta.reportportal.auth.event.UiUserSignedInEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

/**
 * Success handler for external oauth. Generates internal token for authenticated user to be used on UI/Agents side
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	/*
	 * Internal token services facade
	 */
	@Autowired
	private Provider<TokenServicesFacade> tokenServicesFacade;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	OAuthSuccessHandler() {
		super("/");
	}

	@Override
	protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
				OAuth2Authentication oauth = (OAuth2Authentication) authentication;
				OAuth2AccessToken accessToken = tokenServicesFacade.get()
						.createToken(ReportPortalClient.ui,
								oauth.getName(),
								oauth.getUserAuthentication(),
								oauth.getOAuth2Request().getExtensions()
						);

				MultiValueMap<String, String> query = new LinkedMultiValueMap<>();
				query.add("token", accessToken.getValue());
				query.add("token_type", accessToken.getTokenType());
				URI rqUrl = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request))
						.replacePath("/ui/authSuccess.html")
						.replaceQueryParams(query)
						.build()
						.toUri();

				eventPublisher.publishEvent(new UiUserSignedInEvent(authentication));

				getRedirectStrategy().sendRedirect(request, response, rqUrl.toString());
	}
}
