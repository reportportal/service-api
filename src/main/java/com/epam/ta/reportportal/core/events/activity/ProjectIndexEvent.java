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

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.ActivityDetails;

import java.time.LocalDateTime;

/**
 * @author Pavel Bortnik
 */
public class ProjectIndexEvent implements ActivityEvent {

	private Long projectId;
	private String projectName;
	private Long userId;
	private boolean indexing;

	public ProjectIndexEvent() {
	}

	public ProjectIndexEvent(Long projectId, String projectName, Long userId, boolean indexing) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.userId = userId;
		this.indexing = indexing;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public boolean isIndexing() {
		return indexing;
	}

	public void setIndexing(boolean indexing) {
		this.indexing = indexing;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(indexing ? ActivityAction.GENERATE_INDEX.getValue() : ActivityAction.DELETE_INDEX.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.PROJECT);
		activity.setProjectId(projectId);
		activity.setUserId(userId);
		activity.setDetails(new ActivityDetails(projectName));
		return activity;
	}
}
