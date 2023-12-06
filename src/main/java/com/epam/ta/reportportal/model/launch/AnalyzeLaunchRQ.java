/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.model.launch;

import com.epam.ta.reportportal.ws.annotations.In;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyzeLaunchRQ {

	@NotNull
	@JsonProperty(value = "launchId", required = true)
	@Schema
	private Long launchId;

	@NotNull
	@JsonProperty(value = "analyzerMode", required = true)
	@In(allowedValues = { "all", "launch_name", "current_launch", "previous_launch", "current_and_the_same_name" })
	@Schema(allowableValues = "ALL, LAUNCH_NAME, CURRENT_LAUNCH, PREVIOUS_LAUNCH, CURRENT_AND_THE_SAME_NAME")
	private String analyzerHistoryMode;

	@NotNull
	@JsonProperty(value = "analyzerTypeName", required = true)
	@In(allowedValues = { "autoAnalyzer", "patternAnalyzer" })
	@Schema(allowableValues = "autoAnalyzer, patternAnalyzer")
	private String analyzerTypeName;

	@NotNull
	@JsonProperty(value = "analyzeItemsMode", required = true)
	@In(allowedValues = { "to_investigate", "auto_analyzed", "manually_analyzed" })
	@Schema(allowableValues = "TO_INVESTIGATE, AUTO_ANALYZED, MANUALLY_ANALYZED")
	private List<String> analyzeItemsModes;

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}

	public String getAnalyzerHistoryMode() {
		return analyzerHistoryMode;
	}

	public String getAnalyzerTypeName() {
		return analyzerTypeName;
	}

	public void setAnalyzerTypeName(String analyzerTypeName) {
		this.analyzerTypeName = analyzerTypeName;
	}

	public void setAnalyzerHistoryMode(String analyzerHistoryMode) {
		this.analyzerHistoryMode = analyzerHistoryMode;
	}

	public List<String> getAnalyzeItemsModes() {
		return analyzeItemsModes;
	}

	public void setAnalyzeItemsModes(List<String> analyzeItemsModes) {
		this.analyzeItemsModes = analyzeItemsModes;
	}
}
