package com.epam.ta.reportportal.model.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniqueErrorConfig {

	@JsonProperty(value = "isAutoAnalyzerEnabled")
	private boolean enabled;

	@JsonProperty(value = "isAutoAnalyzerEnabled")
	private boolean removeNumbers;

	public UniqueErrorConfig() {
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isRemoveNumbers() {
		return removeNumbers;
	}

	public void setRemoveNumbers(boolean removeNumbers) {
		this.removeNumbers = removeNumbers;
	}
}
