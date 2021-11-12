package com.epam.ta.reportportal.demodata.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DemoLaunch {

	@JsonProperty(value = "suites")
	private List<Suite> suites;

	public DemoLaunch() {
	}

	public List<Suite> getSuites() {
		return suites;
	}

	public void setSuites(List<Suite> suites) {
		this.suites = suites;
	}
}
