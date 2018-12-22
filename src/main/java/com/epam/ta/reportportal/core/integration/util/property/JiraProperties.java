package com.epam.ta.reportportal.core.integration.util.property;

import com.epam.ta.reportportal.entity.integration.IntegrationParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum JiraProperties {

	USER_NAME("username"),
	PASSWORD("password"),
	PROJECT("project"),
	AUTH_TYPE("authType"),
	OAUTH_ACCESS_KEY("oauthAccessKey"),
	URL("url");

	private final String name;

	JiraProperties(String name) {
		this.name = name;
	}

	public Optional<String> getParam(Map<String, Object> params) {
		return Optional.ofNullable(params.get(this.name)).map(o -> (String) o);
	}

	public void setParam(IntegrationParams params, String value) {
		if (null == params.getParams()) {
			params.setParams(new HashMap<>());
		}
		params.getParams().put(this.name, value);
	}
}
