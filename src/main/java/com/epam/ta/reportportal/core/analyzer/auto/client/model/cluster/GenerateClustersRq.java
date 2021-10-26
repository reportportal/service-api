package com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster;

public class GenerateClustersRq {

	private Long launchId;
	private String launchName;

	private Long project;

	private int numberOfLogLines;

	private boolean forUpdate;
	private boolean cleanNumbers;

	public GenerateClustersRq() {
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

	public int getNumberOfLogLines() {
		return numberOfLogLines;
	}

	public void setNumberOfLogLines(int numberOfLogLines) {
		this.numberOfLogLines = numberOfLogLines;
	}

	public boolean isForUpdate() {
		return forUpdate;
	}

	public void setForUpdate(boolean forUpdate) {
		this.forUpdate = forUpdate;
	}

	public boolean isCleanNumbers() {
		return cleanNumbers;
	}

	public void setCleanNumbers(boolean cleanNumbers) {
		this.cleanNumbers = cleanNumbers;
	}
}
