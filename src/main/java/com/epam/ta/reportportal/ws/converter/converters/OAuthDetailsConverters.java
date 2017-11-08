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
import com.google.common.base.Preconditions;

import java.util.function.Function;

/**
 * Converts internal DB model from/to DTO
 *
 * @author Andrei Varabyeu
 */
public final class OAuthDetailsConverters {

	private OAuthDetailsConverters() {
		//static only
	}

	public final static Function<OAuthDetailsResource, OAuth2LoginDetails> FROM_RESOURCE = resource -> {
		Preconditions.checkNotNull(resource);
		OAuth2LoginDetails db = new OAuth2LoginDetails();
		db.setClientAuthenticationScheme(resource.getClientAuthenticationScheme());
		db.setUserAuthorizationUri(resource.getUserAuthorizationUri());
		db.setAccessTokenUri(resource.getAccessTokenUri());
		db.setClientId(resource.getClientId());
		db.setClientSecret(resource.getClientSecret());
		db.setGrantType(resource.getGrantType());
		db.setScope(resource.getScope());
		db.setRestrictions(resource.getRestrictions());
		db.setAuthenticationScheme(resource.getAuthenticationScheme());
		db.setTokenName(resource.getTokenName());
		return db;
	};

	public final static Function<OAuth2LoginDetails, OAuthDetailsResource> TO_RESOURCE = db -> {
		Preconditions.checkNotNull(db);
		OAuthDetailsResource resource = new OAuthDetailsResource();
		resource.setClientAuthenticationScheme(db.getClientAuthenticationScheme());
		resource.setUserAuthorizationUri(db.getUserAuthorizationUri());
		resource.setAccessTokenUri(db.getAccessTokenUri());
		resource.setClientId(db.getClientId());
		resource.setClientSecret(db.getClientSecret());
		resource.setGrantType(db.getGrantType());
		resource.setScope(db.getScope());
		resource.setRestrictions(db.getRestrictions());
		resource.setAuthenticationScheme(db.getAuthenticationScheme());
		resource.setTokenName(db.getTokenName());
		return resource;
	};
}
