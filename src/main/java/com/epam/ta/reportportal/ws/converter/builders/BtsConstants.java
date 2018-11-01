package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.integration.IntegrationParams;

import java.util.HashMap;
import java.util.Optional;

/**
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public enum BtsConstants {
	PROJECT("project"),
	URL("url"),
	DEFECT_FORM_FIELDS("defectFormFields"),
	USER_NAME("username"),
	PASSWORD("password"),
	AUTH_TYPE("authType"),
	OAUTH_ACCESS_KEY("oauthAccessKey");

	private final String name;

	BtsConstants(String name) {
		this.name = name;
	}

	public <T> Optional<T> getParam(IntegrationParams params, Class<T> type) {
		return Optional.ofNullable(params.getParams().get(this.name)).map(type::cast);
	}

	public <T> Optional<Object> getParam(IntegrationParams params) {
		return Optional.ofNullable(params.getParams().get(this.name));
	}

	public void setParam(IntegrationParams params, Object value) {
		if (null == params.getParams()) {
			params.setParams(new HashMap<>());
		}
		params.getParams().put(this.name, value);
	}
}
