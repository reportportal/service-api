package com.epam.ta.reportportal.demodata.model;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class DemoItemMetadata {

	private String name;

	private String parentId;

	private String launchId;

	private boolean retry;

	private boolean nested;

	private TestItemTypeEnum type;

	private ReportPortalUser user;

	private ReportPortalUser.ProjectDetails projectDetails;

	public DemoItemMetadata withName(String name) {
		this.name = name;
		return this;
	}

	public DemoItemMetadata withParentId(String parentId) {
		this.parentId = parentId;
		return this;
	}

	public DemoItemMetadata withLaunch(String launch) {
		this.launchId = launch;
		return this;
	}

	public DemoItemMetadata withRetry(boolean retry) {
		this.retry = retry;
		return this;
	}

	public DemoItemMetadata withNested(boolean nested) {
		this.nested = nested;
		return this;
	}

	public DemoItemMetadata withType(TestItemTypeEnum type) {
		this.type = type;
		return this;
	}

	public DemoItemMetadata withUser(ReportPortalUser user) {
		this.user = user;
		return this;
	}

	public DemoItemMetadata withProjectDetails(ReportPortalUser.ProjectDetails projectDetails) {
		this.projectDetails = projectDetails;
		return this;
	}

	public String getName() {
		return name;
	}

	public String getParentId() {
		return parentId;
	}

	public String getLaunchId() {
		return launchId;
	}

	public boolean isRetry() {
		return retry;
	}

	public boolean isNested() {
		return nested;
	}

	public TestItemTypeEnum getType() {
		return type;
	}

	public ReportPortalUser getUser() {
		return user;
	}

	public ReportPortalUser.ProjectDetails getProjectDetails() {
		return projectDetails;
	}
}
