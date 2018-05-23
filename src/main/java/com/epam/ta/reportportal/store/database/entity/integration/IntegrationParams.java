package com.epam.ta.reportportal.store.database.entity.integration;

import com.epam.ta.reportportal.store.database.entity.JsonbObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Map;

@JsonTypeName("params")
public class IntegrationParams extends JsonbObject {

	private Map<String, Object> params;

	@JsonCreator
	public IntegrationParams(@JsonProperty("params") Map<String, Object> params) {

		this.params = params;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
}
