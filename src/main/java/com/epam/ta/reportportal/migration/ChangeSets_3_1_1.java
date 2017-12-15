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

package com.epam.ta.reportportal.migration;

import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.DBObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.IOException;

/**
 * v.3.1 Migration scripts
 */
@ChangeLog(order = "3.1.1")
public class ChangeSets_3_1_1 {

	private static final String LEAD_ROLE = "LEAD";
	private static final String ID_FIELD = "_id";

	@ChangeSet(order = "3.1.1-1", id = "v3.1.1-Remove LEAD project role", author = "avarabyeu")
	public void removeLeadRole(MongoTemplate mongoTemplate) {
		final Query q = new Query();
		q.fields().include("_id").include("users");
		mongoTemplate.stream(q, DBObject.class, "project").forEachRemaining(dbo -> {
			if (null != dbo.get("users")) {
				DBObject users = (DBObject) dbo.get("users");
				users.keySet().forEach(username -> {
					DBObject user = (DBObject) users.get(username);
					Update u = new Update();
					boolean requireUpdate = false;
					if (LEAD_ROLE.equals(user.get("proposedRole"))) {
						u.set(String.format("users.%s.proposedRole", username), ProjectRole.PROJECT_MANAGER.toString());
						requireUpdate = true;
					}

					if (LEAD_ROLE.equals(user.get("projectRole"))) {
						u.set(String.format("users.%s.projectRole", username), ProjectRole.PROJECT_MANAGER.toString());
						requireUpdate = true;
					}

					if (requireUpdate) {
						mongoTemplate.updateFirst(Query.query(Criteria.where(ID_FIELD).is(dbo.get(ID_FIELD))), u, "project");
					}
				});
			}
		});
	}

}
