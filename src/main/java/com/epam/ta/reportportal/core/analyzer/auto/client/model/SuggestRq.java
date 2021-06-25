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
package com.epam.ta.reportportal.core.analyzer.auto.client.model;

import com.epam.ta.reportportal.ws.model.analyzer.IndexLog;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;

import java.util.Set;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class SuggestRq {

	private Long testItemId;

	private String uniqueId;

	private Integer testCaseHash;

	private Long launchId;

	private String launchName;

	private Long project;

	private AnalyzerConfig analyzerConfig;

	private Set<IndexLog> logs;

	public SuggestRq() {
	}

	public Long getTestItemId() {
		return testItemId;
	}

	public void setTestItemId(Long testItemId) {
		this.testItemId = testItemId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public Integer getTestCaseHash() {
		return testCaseHash;
	}

	public void setTestCaseHash(Integer testCaseHash) {
		this.testCaseHash = testCaseHash;
	}

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}

	public String getLaunchName() {
		return launchName;
	}

	public void setLaunchName(String launchName) {
		this.launchName = launchName;
	}

	public Long getProject() {
		return project;
	}

	public void setProject(Long project) {
		this.project = project;
	}

	public AnalyzerConfig getAnalyzerConfig() {
		return analyzerConfig;
	}

	public void setAnalyzerConfig(AnalyzerConfig analyzerConfig) {
		this.analyzerConfig = analyzerConfig;
	}

	public Set<IndexLog> getLogs() {
		return logs;
	}

	public void setLogs(Set<IndexLog> logs) {
		this.logs = logs;
	}
}
