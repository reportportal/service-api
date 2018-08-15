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

import com.epam.ta.reportportal.database.entity.AnalyzeMode;
import com.epam.ta.reportportal.database.entity.ProjectAnalyzerConfig;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.database.entity.AnalyzeMode.BY_LAUNCH_NAME;
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

	@ChangeSet(order = "4.0-4", id = "v4.1-Update metadata project names to lower case", author = "pbortnik")
	public void updateAttachmentsMetadata(MongoTemplate mongoTemplate) {
		String collection = "fs.files";
		Query q = query(where("metadata.project").exists(true));
		q.fields().include("_id");
		q.fields().include("metadata.project");
		mongoTemplate.stream(q, DBObject.class, collection).forEachRemaining(o -> {
			Map<String, String> metadata = (Map<String, String>) o.get("metadata");
			String project = metadata.get("project");
			if (null != project && !StringUtils.isAllLowerCase(project)) {
				String inLower = project.toLowerCase();
				Update update = new Update();
				update.set("metadata.project", inLower);
				mongoTemplate.updateFirst(query(where("_id").is(o.get("_id"))), update, collection);
			}
		});

	}

	@ChangeSet(order = "4.2-1", id = "v4.2-Update activities names", author = "pbortnik")
	public void updateActivitiesNames(MongoTemplate mongoTemplate) {
		String collection = "activity";
		Query q = query(where("actionType").in(Lists.newArrayList("load_issue", "load_issue_aa", "attach_issue", "attach_issue_aa")));
		q.fields().include(("_id"));
		q.fields().include("actionType");
		mongoTemplate.stream(q, DBObject.class, collection).forEachRemaining(o -> {
			String type = (String) o.get("actionType");
			Update u = new Update();
			if ("load_issue".equals(type) || "attach_issue".equals(type)) {
				u.set("actionType", "link_issue");
			} else if ("load_issue_aa".equals(type) || "attach_issue_aa".equals(type)) {
				u.set("actionType", "link_issue_aa");
			}
			mongoTemplate.updateFirst(query(where("_id").is(o.get("_id"))), u, collection);
		});
	}

	@ChangeSet(order = "4.2-2", id = "v4.2-Introduce default analyzer parameters for each project", author = "pbortnik")
	public void introduceAnalyzerParameters(MongoTemplate mongoTemplate) {
		String collection = "project";
		Query query = query(where("configuration").exists(true).and("configuration.analyzerConfig").exists(false));
		query.fields().include("_id");
		query.fields().include("configuration");
		mongoTemplate.stream(query, DBObject.class, collection).forEachRemaining(p -> {
			BasicDBObject configuration = (BasicDBObject) p.get("configuration");
			Boolean isAAEnabled = (Boolean) Optional.ofNullable(configuration.get("isAutoAnalyzerEnabled")).orElse(false);

			String analyzerMode;
			if (configuration.get("analyzerMode") != null) {
				analyzerMode = AnalyzeMode.valueOf((String) configuration.get("analyzerMode")).getValue();
			} else {
				analyzerMode = BY_LAUNCH_NAME.getValue();
			}

			Update update = new Update();
			update.unset("configuration.isAutoAnalyzerEnabled");
			update.unset("configuration.analyzerMode");

			update.set("configuration.analyzerConfig.minDocFreq", ProjectAnalyzerConfig.MIN_DOC_FREQ);
			update.set("configuration.analyzerConfig.minTermFreq", ProjectAnalyzerConfig.MIN_TERM_FREQ);
			update.set("configuration.analyzerConfig.minShouldMatch", ProjectAnalyzerConfig.MIN_SHOULD_MATCH);
			update.set("configuration.analyzerConfig.numberOfLogLines", ProjectAnalyzerConfig.NUMBER_OF_LOG_LINES);
			update.set("configuration.analyzerConfig.isAutoAnalyzerEnabled", isAAEnabled);
			update.set("configuration.analyzerConfig.analyzerMode", analyzerMode);
			mongoTemplate.updateFirst(query(where("_id").is(p.get("_id"))), update, collection);
		});
	}
}
