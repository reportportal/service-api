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
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author Pavel Bortnik
 */
@ChangeLog(order = "4.0")
public class ChangeSets_4_0 {

	@ChangeSet(order = "4.0-1", id = "v.4.0-Update refactored widgets", author = "pbortnik")
	public void updateWidgets(MongoTemplate mongoTemplate) {
		final String collection = "widget";
		Query query = query(where("contentOptions.type").is("line_chart").and("contentOptions.gadgetType").is("old_line_chart"));
		query.fields().include("_id");
		mongoTemplate.stream(query, DBObject.class, collection).forEachRemaining(widget -> {
			Update update = new Update();
			update.set("contentOptions.type", "trends_chart");
			update.set("contentOptions.gadgetType", "statistic_trend");
			update.set("contentOptions.widgetOptions", new BasicDBObject("viewMode", new String[]{"barMode"}));
			mongoTemplate.updateFirst(query(where("_id").is(widget.get("_id"))), update, collection);
		});
	}

}
