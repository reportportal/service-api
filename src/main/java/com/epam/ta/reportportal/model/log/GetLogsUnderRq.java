package com.epam.ta.reportportal.model.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotNull;

public class GetLogsUnderRq {

	@NotNull
	@JsonProperty(value = "itemIds")
	private List<Long> itemIds;

	@NotNull
	@JsonProperty(value = "logLevel")
	@ApiModelProperty(allowableValues = "error, warn, info, debug, trace, fatal, unknown")
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
