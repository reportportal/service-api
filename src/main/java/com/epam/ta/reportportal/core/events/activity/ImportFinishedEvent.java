/*
 * Copyright 2017 EPAM Systems
 *
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
 */

package com.epam.ta.reportportal.core.events.activity;

/**
 * @author Pavel Bortnik
 */
public class ImportFinishedEvent {

	private Long projectId;

	private Long userId;

	private String fileName;

	public ImportFinishedEvent(Long projectId, Long userId, String fileName) {
		this.projectId = projectId;
		this.userId = userId;
		this.fileName = fileName;
	}

	public Long getProjectId() {
		return projectId;
	}

	public Long getUserId() {
		return userId;
	}

	public String getFileName() {
		return fileName;
	}

}
