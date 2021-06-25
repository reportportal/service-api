package com.epam.ta.reportportal.core.analyzer.auto.client.model;

import com.epam.ta.reportportal.ws.model.analyzer.IndexLog;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class SuggestRq implements Serializable {

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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SuggestRq suggestRq = (SuggestRq) o;
		return Objects.equals(testItemId, suggestRq.testItemId) && Objects.equals(uniqueId, suggestRq.uniqueId) && Objects.equals(
				testCaseHash,
				suggestRq.testCaseHash
		) && Objects.equals(launchId, suggestRq.launchId) && Objects.equals(
				launchName,
				suggestRq.launchName
		) && Objects.equals(project, suggestRq.project) && Objects.equals(analyzerConfig, suggestRq.analyzerConfig) && Objects.equals(
				logs,
				suggestRq.logs
		);
	}

	@Override
	public int hashCode() {
		return Objects.hash(testItemId, uniqueId, testCaseHash, launchId, launchName, project, analyzerConfig, logs);
	}

	@Override
	public String toString() {
		return "SuggestRq{" + "testItemId=" + testItemId + ", uniqueId='" + uniqueId + '\'' + ", testCaseHash=" + testCaseHash
				+ ", launchId=" + launchId + ", launchName='" + launchName + '\'' + ", project=" + project + ", analyzerConfig="
				+ analyzerConfig + ", logs=" + logs + '}';
	}
}
