/*
 * Copyright 2016 EPAM Systems
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

import com.epam.ta.reportportal.database.entity.sharing.AclEntry;
import com.epam.ta.reportportal.database.entity.sharing.AclPermissions;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;

/**
 * Extension of {@link Builder}. This builder add possibility to build sharable resources
 *
 * @param <T>
 * @author Aliaksei_Makayed
 */
public abstract class ShareableEntityBuilder<T extends Shareable> extends Builder<T> {

	public abstract ShareableEntityBuilder<T> addSharing(String owner, String project, String description, boolean isShare);

	protected void addAcl(String owner, String project, String description, boolean isShare) {
		if (owner != null && project != null) {
			getObject().setOwner(owner);
			getObject().setDescription(description);
			if (isShare) {
				AclEntry aclEntry = new AclEntry();
				aclEntry.setProjectId(project);
				aclEntry.addPermission(AclPermissions.READ);
				getObject().addAclEntry(aclEntry);
			}
		}
	}

}