/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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
 
package com.epam.ta.reportportal.ws.converter.builders;

import java.util.Date;
import java.util.Set;

import com.google.common.collect.Sets;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;

@Service
@Scope("prototype")
public class LaunchBuilder extends Builder<Launch> {

	public LaunchBuilder addStartRQ(StartLaunchRQ request) {
		if (request != null) {
			getObject().setStartTime(request.getStartTime());
			getObject().setName(request.getName().trim());
			if (null != request.getDescription())
				getObject().setDescription(request.getDescription().trim());
			Set<String> tags = request.getTags();
			if (null != tags) {
				tags = Sets.newHashSet(EntityUtils.trimStrings(EntityUtils.update(tags)));
			}
			getObject().setTags(tags);
			if (request.getMode() != null) {
				getObject().setMode(request.getMode());
			}
		}
		return this;
	}

	public LaunchBuilder addStatus(Status status) {
		getObject().setStatus(status);
		return this;
	}

	public LaunchBuilder addUser(String userName) {
		getObject().setUserRef(userName);
		return this;
	}

	public LaunchBuilder addProject(String projectName) {
		getObject().setProjectRef(projectName);
		return this;
	}

	public LaunchBuilder addEndTime(Date endTime) {
		getObject().setEndTime(endTime);
		return this;
	}

	@Override
	protected Launch initObject() {
		return new Launch();
	}
}