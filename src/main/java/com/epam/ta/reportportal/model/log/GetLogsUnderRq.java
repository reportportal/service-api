package com.epam.ta.reportportal.model.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import jakarta.validation.constraints.NotNull;

public class GetLogsUnderRq {

	@NotNull
	@JsonProperty(value = "itemIds")
	private List<Long> itemIds;

	@NotNull
	@JsonProperty(value = "logLevel")
	@Schema(allowableValues = "error, warn, info, debug, trace, fatal, unknown")
	private String logLevel;

	public GetLogsUnderRq() {
	}

	public List<Long> getItemIds() {
		return itemIds;
	}

	public void setItemIds(List<Long> itemIds) {
		this.itemIds = itemIds;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
}
