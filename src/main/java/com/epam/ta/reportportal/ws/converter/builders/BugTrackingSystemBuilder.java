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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystemAuth;
import com.epam.ta.reportportal.ws.model.externalsystem.CreateExternalSystemRQ;

import java.util.function.Supplier;

/**
 * @author Pavel Bortnik
 */
public class BugTrackingSystemBuilder implements Supplier<BugTrackingSystem> {

		private BugTrackingSystem bugTrackingSystem;
	
		public BugTrackingSystemBuilder() {
			bugTrackingSystem = new BugTrackingSystem();
		}

		public BugTrackingSystemBuilder addExternalSystem(CreateExternalSystemRQ rq) {
			bugTrackingSystem.setUrl(rq.getUrl());
			bugTrackingSystem.setBtsType(rq.getExternalSystemType());
			bugTrackingSystem.setBtsProject(rq.getProject());
			return this;
		}

		public BugTrackingSystemBuilder addSystemAuth(BugTrackingSystemAuth auth) {
			bugTrackingSystem.setAuth(auth);
			return this;
		}

		public BugTrackingSystemBuilder addProjectId(Long projectId) {
			bugTrackingSystem.setProjectId(projectId);
			return this;
		}

	@Override
	public BugTrackingSystem get() {
		return bugTrackingSystem;
	}
}
