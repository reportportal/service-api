package com.epam.ta.reportportal.demodata.model;

import com.epam.ta.reportportal.commons.ReportPortalUser;

public class RootMetaData {

	private final String launchUuid;
	private final ReportPortalUser user;
	private final ReportPortalUser.ProjectDetails projectDetails;

	private RootMetaData(String launchUuid, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		this.launchUuid = launchUuid;
		this.user = user;
		this.projectDetails = projectDetails;
	}

	public String getLaunchUuid() {
		return launchUuid;
	}

	public ReportPortalUser getUser() {
		return user;
	}

	public ReportPortalUser.ProjectDetails getProjectDetails() {
		return projectDetails;
	}

	public static RootMetaData of(String launchUuid, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		return new RootMetaData(launchUuid, user, projectDetails);
	}
}
