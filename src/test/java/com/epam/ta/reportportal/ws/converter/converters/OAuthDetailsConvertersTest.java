/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.settings.OAuth2LoginDetails;
import com.epam.ta.reportportal.ws.model.settings.OAuthDetailsResource;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * @author Andrei Varabyeu
 */
public class OAuthDetailsConvertersTest {

	@Test
	public void testToResource() {
		OAuth2LoginDetails oAuth2LoginDetails = new OAuth2LoginDetails();
		oAuth2LoginDetails.setScope(Collections.singletonList("user"));
		oAuth2LoginDetails.setGrantType("authorization_code");
		oAuth2LoginDetails.setClientId("f4cec43d4541283879c4");
		oAuth2LoginDetails.setClientSecret("a31aa6de3e27c11d90762cad11936727d6b0759e");
		oAuth2LoginDetails.setAccessTokenUri("https://github.com/login/oauth/access_token");
		oAuth2LoginDetails.setUserAuthorizationUri("https://github.com/login/oauth/authorize");
		oAuth2LoginDetails.setClientAuthenticationScheme("form");

		validate(OAuthDetailsConverters.TO_RESOURCE.apply(oAuth2LoginDetails), oAuth2LoginDetails);
	}

	@Test
	public void testFromResource() {
		OAuthDetailsResource oAuth2LoginDetails = new OAuthDetailsResource();
		oAuth2LoginDetails.setScope(Collections.singletonList("user"));
		oAuth2LoginDetails.setGrantType("authorization_code");
		oAuth2LoginDetails.setClientId("f4cec43d4541283879c4");
		oAuth2LoginDetails.setClientSecret("a31aa6de3e27c11d90762cad11936727d6b0759e");
		oAuth2LoginDetails.setAccessTokenUri("https://github.com/login/oauth/access_token");
		oAuth2LoginDetails.setUserAuthorizationUri("https://github.com/login/oauth/authorize");
		oAuth2LoginDetails.setClientAuthenticationScheme("form");

		validate(oAuth2LoginDetails, OAuthDetailsConverters.FROM_RESOURCE.apply(oAuth2LoginDetails));
	}

	private void validate(OAuthDetailsResource resource, OAuth2LoginDetails db) {
		Assert.assertThat("Incorrect token uri", resource.getAccessTokenUri(), Matchers.equalTo(db.getAccessTokenUri()));
		Assert.assertThat("Incorrect auth scheme", resource.getAuthenticationScheme(), Matchers.equalTo(db.getAuthenticationScheme()));
		Assert.assertThat("Incorrect client auth scheme", resource.getClientAuthenticationScheme(),
				Matchers.equalTo(db.getClientAuthenticationScheme())
		);
		Assert.assertThat("Incorrect client ID", resource.getClientId(), Matchers.equalTo(db.getClientId()));
		Assert.assertThat("Incorrect client secret", resource.getClientSecret(), Matchers.equalTo(db.getClientSecret()));
		Assert.assertThat("Incorrect grant type", resource.getGrantType(), Matchers.equalTo(db.getGrantType()));
		Assert.assertThat("Incorrect restrictions", resource.getRestrictions(), Matchers.equalTo(db.getRestrictions()));
		Assert.assertThat("Incorrect scope", resource.getScope(), Matchers.equalTo(db.getScope()));
		Assert.assertThat("Incorrect token name", resource.getTokenName(), Matchers.equalTo(db.getTokenName()));
		Assert.assertThat("Incorrect user authorization uri", resource.getUserAuthorizationUri(),
				Matchers.equalTo(db.getUserAuthorizationUri())
		);

	}

}