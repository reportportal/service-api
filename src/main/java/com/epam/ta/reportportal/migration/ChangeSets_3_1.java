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

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.UUID;

/**
 * v.3.1 Migration scripts
 */
@ChangeLog(order = "3.1.0")
public class ChangeSets_3_1 {

	@ChangeSet(order = "3.1.0-1", id = "v3.1.0-Remove debug from email settings", author = "avarabyeu")
	public void removeDebugField(MongoTemplate mongoTemplate) {
		mongoTemplate.updateFirst(Query.query(new Criteria()), new Update().unset("serverEmailDetails.debug"), "serverSettings");
	}

	@ChangeSet(order = "3.1.0-2", id = "v3.1.2-Add instance ID", author = "avarabyeu")
	public void addInstanceID(MongoTemplate mongoTemplate) {
		mongoTemplate.updateFirst(Query.query(Criteria.where("instanceId").exists(false)),
				new Update().set("instanceId", UUID.randomUUID().toString()), "serverSettings"
		);
	}
}
