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

package com.epam.ta.reportportal.migration;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.DBObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.epam.ta.reportportal.database.entity.item.Activity.FieldValues.*;
import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.*;

/**
 * Migration 3.3
 *
 * @author Pavel Bortnik
 */
@ChangeLog(order = "3.3")
public class ChangeSets_3_3 {

	private static final String HISTORY = "history";
	private static final String ID = "_id";
	private static final String COLLECTION = "activity";
	private static final String ACTION_TYPE = "actionType";

	@ChangeSet(order = "3.3-1", id = "v3.3-Replace activities embedded collection 'history' with array", author = "pbortnik")
	public void replaceActivitesHistory(MongoTemplate mongoTemplate) {
		final Query q = new Query(Criteria.where(HISTORY).exists(true));
		q.fields().include(ID).include(HISTORY);
		mongoTemplate.stream(q, DBObject.class, COLLECTION).forEachRemaining(dbo -> {
			DBObject history = (DBObject) dbo.get(HISTORY);
			Update u = new Update();
			Map[] dbArray = new LinkedHashMap[history.keySet().size()];
			int i = 0;
			for (String key : history.keySet()) {
				DBObject o = (DBObject) history.get(key);
				if (null != o && null != o.keySet()) {
					Map res = new LinkedHashMap(o.keySet().size() + 1);
					res.put("field", key);
					res.putAll(o.toMap());
					dbArray[i] = res;
					i++;
				} else {
					Map res = new LinkedHashMap<>(3);
					res.put("field", key);
					res.put(OLD_VALUE, "");
					res.put(NEW_VALUE, "");
					dbArray[i] = res;
					i++;
				}
			}
			u.set(HISTORY, dbArray);
			mongoTemplate.updateFirst(Query.query(Criteria.where(ID).is(dbo.get(ID))), u, COLLECTION);
		});
	}

	@ChangeSet(order = "3.3-2", id = "v3.3-Update activity types with new values", author = "pbortnik")
	public void updateActivityTypes(MongoTemplate mongoTemplate) {
		final Query q = new Query(Criteria.where(ACTION_TYPE).in("start", "finish", "delete", "share", "unshare"));
		mongoTemplate.stream(q, DBObject.class, COLLECTION).forEachRemaining(dbo -> {
			String type = (String) dbo.get(ACTION_TYPE);
			Update u = new Update();
			switch (type) {
				case "start":
					u.set(ACTION_TYPE, START_LAUNCH.getValue());
					break;
				case "finish":
					u.set(ACTION_TYPE, FINISH_LAUNCH.getValue());
					break;
				case "delete":
					u.set(ACTION_TYPE, DELETE_LAUNCH.getValue());
					break;
				case "share":
					u = createShareHistory(u, (String) dbo.get("objectType"), "true", "false");
					break;
				case "unshare":
					u = createShareHistory(u, (String) dbo.get("objectType"), "false", "true");
					break;
			}
			mongoTemplate.updateFirst(Query.query(Criteria.where(ID).is(dbo.get(ID))), u, COLLECTION);
		});
	}

	@ChangeSet(order = "3.3-3", id = "v3.3-Generate uniqueId for all test items based on md5 algorithm", author = "pbortnik")
	public void generate(MongoTemplate mongoTemplate) {
		mongoTemplate.createCollection("generationCheckpoint");
	}

	@ChangeSet(order = "3.3-4", id = "V3.3-Drop unused failReferences collection in reason of new analyzer", author = "pbortnik")
	public void dropfailReferences(MongoTemplate mongoTemplate) {
		mongoTemplate.dropCollection("failReferences");
	}

	private Update createShareHistory(Update u, String objectType, String oldValue, String newValue) {
		Map[] dbArray = new LinkedHashMap[1];
		Map res = new LinkedHashMap(3);
		res.put(FIELD, "share");
		res.put(OLD_VALUE, oldValue);
		res.put(NEW_VALUE, newValue);
		dbArray[0] = res;
		u.set("history", dbArray);
		switch (objectType) {
			case "dashboard":
				u.set(ACTION_TYPE, UPDATE_DASHBOARD.getValue());
				break;
			case "widget":
				u.set(ACTION_TYPE, UPDATE_WIDGET.getValue());
				break;
		}
		return u;
	}
}
