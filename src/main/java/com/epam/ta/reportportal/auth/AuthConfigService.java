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

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Builds proxy instance of {@link RestTemplate} which load OAuth resouce details from DB on each operation
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Component
public class AuthConfigService {
//
//	@Autowired
//	private ServerSettingsRepository serverSettingsRepository;
//
//	/**
//	 * Builds proxy instance of {@link RestTemplate} which load OAuth resouce details from DB on each operation
//	 *
//	 * @param name                Name/ID of resource of {@link RestTemplate}
//	 * @param oauth2ClientContext OAuth Client context
//	 * @return Proxy instance of {@link RestTemplate}
//	 */
//	public OAuth2RestOperations getRestTemplate(String name, OAuth2ClientContext oauth2ClientContext) {
//		return newProxy(OAuth2RestOperations.class, (proxy, method, args) -> {
//			try {
//				return method.invoke(new OAuth2RestTemplate(loadResourceDetails(name), oauth2ClientContext), args);
//
//			} catch (InvocationTargetException e) {
//				throw e.getTargetException();
//			}
//		});
//	}
//
//	public Supplier<OAuth2LoginDetails> getLoginDetailsSupplier(String name) {
//		return () -> loadLoginDetails(name).orElseThrow(() -> noAuthDetailsException(name));
//	}
//
//	/**
//	 * Loads {@link OAuth2LoginDetails} from database
//	 *
//	 * @param name Name of resource
//	 * @return Built {@link OAuth2ProtectedResourceDetails}
//	 */
//	public Optional<OAuth2LoginDetails> loadLoginDetails(String name) {
//		return ofNullable(serverSettingsRepository.findOne("default"))
//				.map(ServerSettings::getoAuth2LoginDetails)
//				.flatMap(details -> ofNullable(details.get(name)));
//	}
//
//	/**
//	 * Loads {@link OAuth2ProtectedResourceDetails} from database
//	 *
//	 * @param name Name of resource
//	 * @return Built {@link OAuth2ProtectedResourceDetails}
//	 */
//	public OAuth2ProtectedResourceDetails loadResourceDetails(String name) {
//		return loadLoginDetails(name).map(RESOURCE_DETAILS_CONVERTER).orElseThrow(() -> noAuthDetailsException(name));
//	}
//
//	/**
//	 * Converts DB model to {@link OAuth2ProtectedResourceDetails}
//	 */
//	private static final Function<OAuth2LoginDetails, OAuth2ProtectedResourceDetails> RESOURCE_DETAILS_CONVERTER = d -> {
//		BaseOAuth2ProtectedResourceDetails details;
//
//		String grantType = d.getGrantType();
//		switch (grantType) {
//		case "authorization_code":
//			details = new AuthorizationCodeResourceDetails();
//			break;
//		case "implicit":
//			details = new ImplicitResourceDetails();
//			break;
//		case "client_credentials":
//			details = new ClientCredentialsResourceDetails();
//			break;
//		case "password":
//			details = new ResourceOwnerPasswordResourceDetails();
//			break;
//		default:
//			details = new BaseOAuth2ProtectedResourceDetails();
//		}
//
//		if (null != d.getUserAuthorizationUri()) {
//			((AbstractRedirectResourceDetails) details).setUserAuthorizationUri(d.getUserAuthorizationUri());
//		}
//
//		details.setAccessTokenUri(d.getAccessTokenUri());
//		if (null != d.getAuthenticationScheme()) {
//			details.setAuthenticationScheme(AuthenticationScheme.valueOf(d.getAuthenticationScheme()));
//		}
//
//		details.setAuthenticationScheme(ofNullable(d.getAuthenticationScheme()).map(AuthenticationScheme::valueOf).orElse(null));
//
//		details.setClientAuthenticationScheme(
//				ofNullable(d.getClientAuthenticationScheme()).map(AuthenticationScheme::valueOf).orElse(null));
//
//		details.setClientId(d.getClientId());
//		details.setClientSecret(d.getClientSecret());
//		details.setScope(d.getScope());
//		details.setTokenName(d.getTokenName());
//		return details;
//	};
//
//	private ProviderNotFoundException noAuthDetailsException(String name) {
//		return new ProviderNotFoundException("Auth details '" + name + "' are not configured");
//	}
}
