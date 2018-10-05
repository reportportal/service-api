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

package com.epam.ta.reportportal.ws.converter.converters;

/**
 * Converts internal DB model from/to DTO
 *
 * @author Andrei Varabyeu
 */
public final class OAuthDetailsConverters {

	private OAuthDetailsConverters() {
		//static only
	}

	//	public final static Function<OAuthDetailsResource, OAuth2LoginDetails> FROM_RESOURCE = resource -> {
	//		Preconditions.checkNotNull(resource);
	//		OAuth2LoginDetails db = new OAuth2LoginDetails();
	//		db.setClientAuthenticationScheme(resource.getClientAuthenticationScheme());
	//		db.setUserAuthorizationUri(resource.getUserAuthorizationUri());
	//		db.setAccessTokenUri(resource.getAccessTokenUri());
	//		db.setClientId(resource.getClientId());
	//		db.setClientSecret(resource.getClientSecret());
	//		db.setGrantType(resource.getGrantType());
	//		db.setScope(resource.getScope());
	//		db.setRestrictions(resource.getRestrictions());
	//		db.setAuthenticationScheme(resource.getAuthenticationScheme());
	//		db.setTokenName(resource.getTokenName());
	//		return db;
	//	};
	//
	//	public final static Function<OAuth2LoginDetails, OAuthDetailsResource> TO_RESOURCE = db -> {
	//		Preconditions.checkNotNull(db);
	//		OAuthDetailsResource resource = new OAuthDetailsResource();
	//		resource.setClientAuthenticationScheme(db.getClientAuthenticationScheme());
	//		resource.setUserAuthorizationUri(db.getUserAuthorizationUri());
	//		resource.setAccessTokenUri(db.getAccessTokenUri());
	//		resource.setClientId(db.getClientId());
	//		resource.setClientSecret(db.getClientSecret());
	//		resource.setGrantType(db.getGrantType());
	//		resource.setScope(db.getScope());
	//		resource.setRestrictions(db.getRestrictions());
	//		resource.setAuthenticationScheme(db.getAuthenticationScheme());
	//		resource.setTokenName(db.getTokenName());
	//		return resource;
	//	};
}
