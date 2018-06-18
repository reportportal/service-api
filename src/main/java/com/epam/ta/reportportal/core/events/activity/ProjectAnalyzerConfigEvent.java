/*
 * Copyright 2017 EPAM Systems
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.database.entity.ProjectAnalyzerConfig;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;

/**
 * @author Pavel Bortnik
 */
public class ProjectAnalyzerConfigEvent extends BeforeEvent<ProjectAnalyzerConfig> {

	private String projectRef;

	private String updatedBy;

	private AnalyzerConfig analyzerConfig;

	public ProjectAnalyzerConfigEvent(ProjectAnalyzerConfig before, String projectRef, String updatedBy, AnalyzerConfig analyzerConfig) {
		super(before);
		this.projectRef = projectRef;
		this.updatedBy = updatedBy;
		this.analyzerConfig = analyzerConfig;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public AnalyzerConfig getAnalyzerConfig() {
		return analyzerConfig;
	}

	public void setAnalyzerConfig(AnalyzerConfig analyzerConfig) {
		this.analyzerConfig = analyzerConfig;
	}

	public String getProjectRef() {
		return projectRef;
	}

	public void setProjectRef(String projectRef) {
		this.projectRef = projectRef;
	}
}
