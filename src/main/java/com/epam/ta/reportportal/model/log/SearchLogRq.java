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

package com.epam.ta.reportportal.model.log;

import com.epam.ta.reportportal.ws.annotations.In;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchLogRq {

	@NotNull
	@JsonProperty(value = "searchMode", required = true)
	@In(allowedValues = { "launchName", "currentLaunch", "filer" })
	@Schema(allowableValues = "currentLaunch, launchName, filter")
	private String searchMode;

	private Long filterId;

	public String getSearchMode() {
		return searchMode;
	}

	public void setSearchMode(String searchMode) {
		this.searchMode = searchMode;
	}

	public Long getFilterId() {
		return filterId;
	}

	public void setFilterId(Long filterId) {
		this.filterId = filterId;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("SearchLogRq{");
		sb.append("searchMode='").append(searchMode).append('\'');
		sb.append(", filterId=").append(filterId);
		sb.append('}');
		return sb.toString();
	}
}
