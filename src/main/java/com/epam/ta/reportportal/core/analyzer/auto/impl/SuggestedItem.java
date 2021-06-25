/*
 * Copyright 2021 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.analyzer.auto.impl;

import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestInfo;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuggestedItem {

	private TestItemResource testItemResource;

	private Set<LogResource> logs;

	private SuggestInfo suggestInfo;

	public Set<LogResource> getLogs() {
		return logs;
	}

	public void setLogs(Set<LogResource> logs) {
		this.logs = logs;
	}

	public TestItemResource getTestItemResource() {
		return testItemResource;
	}

	public void setTestItemResource(TestItemResource testItemResource) {
		this.testItemResource = testItemResource;
	}

	public SuggestInfo getSuggestRs() {
		return suggestInfo;
	}

	public void setSuggestRs(SuggestInfo suggestInfo) {
		this.suggestInfo = suggestInfo;
	}
}
