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

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.auth.UatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2ClientProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

/**
 * Configuration of clients for other services
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Configuration
public class ServicesConfiguration {

	@Autowired
	private OAuth2ClientProperties oauthClientProperties;

	@LoadBalanced
	@Bean
	public OAuth2RestTemplate rpInternalRestTemplate(OAuth2ProtectedResourceDetails resource, OAuth2ClientContext context) {
		ClientCredentialsResourceDetails clientCredentialsResourceDetails = new ClientCredentialsResourceDetails();
		clientCredentialsResourceDetails.setClientId(oauthClientProperties.getClientId());
		clientCredentialsResourceDetails.setClientSecret(oauthClientProperties.getClientSecret());
		clientCredentialsResourceDetails.setAccessTokenUri(resource.getAccessTokenUri());
		clientCredentialsResourceDetails.setScope(resource.getScope());
		return new OAuth2RestTemplate(clientCredentialsResourceDetails, context);
	}

	@Bean
	public UatClient uatClient(@Value("${rp.uat.serviceUrl}") String uatServiceUrl,
			@Qualifier("rpInternalRestTemplate") OAuth2RestTemplate restTemplate) {
		return new UatClient(uatServiceUrl, restTemplate);
	}
}
