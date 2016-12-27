package com.epam.ta.reportportal.ws.converter;

import com.epam.ta.reportportal.database.entity.OAuth2LoginDetails;
import com.epam.ta.reportportal.ws.model.settings.OAuthDetailsResource;

import java.util.Map;
import java.util.function.Function;

/**
 * Converts internal DB model from/to DTO
 *
 * @author Andrei Varabyeu
 */
public class OAuthDetailsConverters {

	public static Function<OAuthDetailsResource, OAuth2LoginDetails> FROM_RESOURCE = resource -> {
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

	public static Function<OAuth2LoginDetails, OAuthDetailsResource> TO_RESOURCE = db -> {
		OAuthDetailsResource resource = new OAuthDetailsResource();
		resource.setClientAuthenticationScheme(db.getClientAuthenticationScheme());
		resource.setUserAuthorizationUri(db.getUserAuthorizationUri());
		resource.setAccessTokenUri(db.getAccessTokenUri());
		resource.setClientId(db.getClientId());
		resource.setClientSecret(db.getClientSecret());
		resource.setGrantType(db.getGrantType());
		resource.setScope(db.getScope());
		resource.setRestrictions((Map<String, String>) db.getRestrictions());
		resource.setAuthenticationScheme(db.getAuthenticationScheme());
		resource.setTokenName(db.getTokenName());
		return resource;
	};
}
