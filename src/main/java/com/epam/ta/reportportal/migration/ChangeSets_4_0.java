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
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Map;

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
		Criteria criteria = new Criteria();
		Query query = query(criteria.orOperator(
				where("contentOptions.type").is("line_chart").and("contentOptions.gadgetType").is("old_line_chart"),
				where("contentOptions.type").is("trends_chart").and("contentOptions.gadgetType").is("statistic_trend")
		));
		query.fields().include("_id");
		query.fields().include("contentOptions");
		String[] viewMode = new String[1];
		mongoTemplate.stream(query, DBObject.class, collection).forEachRemaining(widget -> {
			Update update = new Update();
			DBObject contentOptions = (DBObject) widget.get("contentOptions");

			BasicDBObject widgetOptions = (BasicDBObject) contentOptions.get("widgetOptions");
			String type = (String) contentOptions.get("type");

			update.set("contentOptions.type", "trends_chart");
			update.set("contentOptions.gadgetType", "statistic_trend");
			viewMode[0] = "line_chart".equals(type) ? "areaChartMode" : "barMode";
			if (widgetOptions != null) {
				widgetOptions.append("viewMode", viewMode);
			} else {
				widgetOptions = new BasicDBObject("viewMode", viewMode);
			}
			update.set("contentOptions.widgetOptions", widgetOptions);
			mongoTemplate.updateFirst(query(where("_id").is(widget.get("_id"))), update, collection);
		});
	}

	@ChangeSet(order = "4.0-2", id = "v.4.0-Update filter model", author = "pbortnik")
	public void updateFilters(MongoTemplate mongoTemplate) {
		final String collection = "userFilter";
		Query q = query(where("selectionOptions").exists(true));
		q.fields().include("_id");
		q.fields().include("selectionOptions");
		mongoTemplate.stream(q, DBObject.class, collection).forEachRemaining(filter -> {
			Update update = new Update();
			Map<String, Object> map = (Map<String, Object>) filter.get("selectionOptions");

			String sortingColumnName = (String) map.get("sortingColumnName");
			Boolean isAsc = (Boolean) map.get("isAsc");

			List<BasicDBObject> selectionOrders = Lists.newArrayList(
					new BasicDBObject("isAsc", isAsc).append("sortingColumnName", sortingColumnName));

			if (sortingColumnName.equals("start_time")) {
				selectionOrders.add(new BasicDBObject("isAsc", isAsc).append("sortingColumnName", "number"));
			}

			update.set("selectionOptions", new BasicDBObject("pageNumber", map.get("pageNumber")).append("orders", selectionOrders));
			mongoTemplate.updateFirst(query(where("_id").is(filter.get("_id"))), update, collection);
		});
	}

	@ChangeSet(order = "4.0-3", id = "v4.0-Add log indexing checkpoint ID", author = "isharamet")
	public void addLogIndexingCheckpoint(MongoTemplate mongoTemplate) {
		mongoTemplate.createCollection("logIndexingCheckpoint");
	}

}
