package com.epam.ta.reportportal.store.database.entity.integration;

import com.epam.ta.reportportal.store.database.entity.JsonbObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Map;

/**
 * @author Yauheni_Martynau
 */
@JsonTypeName("details")
public class IntegrationTypeDetails extends JsonbObject {

	private Map<String, Object> details;

	@JsonCreator
	public IntegrationTypeDetails(@JsonProperty("details")Map<String, Object> details) {

		this.details = details;
	}

	public Map<String, Object> getDetails() {
		return details;
	}

	public void setDetails(Map<String, Object> details) {
		this.details = details;
	}
}
