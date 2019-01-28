/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.auth.UatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2ClientProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
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
@Conditional(Conditions.NotTestCondition.class)
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
