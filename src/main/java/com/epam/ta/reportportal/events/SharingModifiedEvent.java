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
package com.epam.ta.reportportal.events;

import com.epam.ta.reportportal.database.entity.sharing.Shareable;

import java.util.List;

/**
 * @author Andrei Varabyeu
 */
public class SharingModifiedEvent {

	private final String user;
	private final String project;
	private final boolean share;
	private final List<? extends Shareable> items;

	public SharingModifiedEvent(List<? extends Shareable> items, String user, String project, boolean share) {
		this.user = user;
		this.project = project;
		this.share = share;
		this.items = items;
	}

	public String getUser() {
		return user;
	}

	public String getProject() {
		return project;
	}

	public boolean isShare() {
		return share;
	}

	public List<? extends Shareable> getItems() {
		return items;
	}
}
