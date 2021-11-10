package com.epam.ta.reportportal.demodata.model;

public class Step extends TestingModel {

	private String name;
	private String status;
	private String issue;

	public Step() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIssue() {
		return issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}
}
