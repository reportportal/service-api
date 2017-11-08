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

/**
 * v.2.7 Migration scripts
 */
@ChangeLog(order = "2.7")
public class ChangeSets_2_7 {

	@ChangeSet(order = "2.7-1", id = "v2.7-1-Description", author = "dkavalets")
	public void initLoad(MongoTemplate mongoTemplate) {
		mongoTemplate.getDb()
				.doEval("db.user.find({}).forEach(function(user){\n" + "        var userId = user._id\n" + "        var user = {\n"
						+ "            userId : {\n" + "                    \"proposedRole\" : \"PROJECT_MANAGER\",\n"
						+ "                    \"projectRole\" : \"PROJECT_MANAGER\"\n" + "                }\n" + "            }  \n"
						+ "        var project = {\n" + "             \"_id\" : userId + \"_personal\",\n"
						+ "             \"_class\" : \"com.epam.ta.reportportal.database.entity.Project\",\n"
						+ "             \"addInfo\" : \"Personal project of \" + userId,\n" + "             \"configuration\" : {\n"
						+ "                \"statisticsCalculationStrategy\" : \"TEST_BASED\",\n"
						+ "                \"externalSystem\" : [],\n" + "                \"entryType\" : \"PERSONAL\",\n"
						+ "                \"projectSpecific\" : \"DEFAULT\",\n" + "                \"interruptJobTime\" : \"1 day\",\n"
						+ "                \"keepLogs\" : \"3 months\",\n" + "                \"keepScreenshots\" : \"1 month\",\n"
						+ "                \"isAutoAnalyzerEnabled\" : false,\n" + "                \"subTypes\" : {\n"
						+ "                    \"PRODUCT_BUG\" : [\n" + "                        {\n"
						+ "                            \"locator\" : \"PB001\",\n"
						+ "                            \"typeRef\" : \"PRODUCT_BUG\",\n"
						+ "                            \"longName\" : \"Product Bug\",\n"
						+ "                            \"shortName\" : \"PB\",\n"
						+ "                            \"hexColor\" : \"#ec3900\"\n" + "                        }\n"
						+ "                    ],\n" + "                    \"AUTOMATION_BUG\" : [\n" + "                        {\n"
						+ "                            \"locator\" : \"AB001\",\n"
						+ "                            \"typeRef\" : \"AUTOMATION_BUG\",\n"
						+ "                            \"longName\" : \"Automation Bug\",\n"
						+ "                            \"shortName\" : \"AB\",\n"
						+ "                            \"hexColor\" : \"#f7d63e\"\n" + "                        }\n"
						+ "                        ],\n" + "                    \"TO_INVESTIGATE\" : [\n" + "                        {\n"
						+ "                            \"locator\" : \"TI001\",\n"
						+ "                            \"typeRef\" : \"TO_INVESTIGATE\",\n"
						+ "                            \"longName\" : \"To Investigate\",\n"
						+ "                            \"shortName\" : \"TI\",\n"
						+ "                            \"hexColor\" : \"#ffb743\"\n" + "                        }\n"
						+ "                    ],\n" + "                    \"NO_DEFECT\" : [\n" + "                        {\n"
						+ "                            \"locator\" : \"ND001\",\n"
						+ "                            \"typeRef\" : \"NO_DEFECT\",\n"
						+ "                            \"longName\" : \"No Defect\",\n"
						+ "                            \"shortName\" : \"ND\",\n"
						+ "                            \"hexColor\" : \"#777777\"\n" + "                        }\n"
						+ "                    ],\n" + "                    \"SYSTEM_ISSUE\" : [\n" + "                        {\n"
						+ "                            \"locator\" : \"SI001\",\n"
						+ "                            \"typeRef\" : \"SYSTEM_ISSUE\",\n"
						+ "                            \"longName\" : \"System Issue\",\n"
						+ "                            \"shortName\" : \"SI\",\n"
						+ "                            \"hexColor\" : \"#0274d1\"\n" + "                        }\n"
						+ "                    ]\n" + "                },\n" + "                \"emailConfig\" : {\n"
						+ "                    \"emailEnabled\" : false,\n"
						+ "                    \"from\" : \"reportportal@example.com\",\n" + "                    \"emailCases\" : [\n"
						+ "                        {\n" + "                            \"recipients\" : [\n"
						+ "                                \"OWNER\"\n" + "                            ],\n"
						+ "                            \"sendCase\" : \"ALWAYS\",\n" + "                            \"launchNames\" : [],\n"
						+ "                            \"tags\" : []\n" + "                        }\n" + "                    ]\n"
						+ "                }\n" + "             },\n" + "             \"users\" : {\n" + "            },\n"
						+ "             \"creationDate\" : new ISODate()\n" + "            }\n" + "        project.users[userId] = {\n"
						+ "                    \"proposedRole\" : \"PROJECT_MANAGER\",\n"
						+ "                    \"projectRole\" : \"PROJECT_MANAGER\"\n" + "                }\n"
						+ "        if (!db.project.findOne({\"_id\":project._id})){\n" + "             db.project.save(project)\n"
						+ "        }\n" + "})");
		mongoTemplate.getDb()
				.doEval("db.projectSettings.find({}).forEach(\n" + "    function(doc) {\n"
						+ "            var project = db.project.findOne({'_id':doc._id})\n" + "            if (project != null) {"
						+ "                project.configuration.subTypes = doc.subTypes\n" + "                db.project.save(project)\n"
						+ "            }\n" + "    }\n" + ")");
		mongoTemplate.getDb().doEval("db.projectSettings.drop()");
		mongoTemplate.getDb()
				.doEval("db.user.find({}).forEach(\n" + "    function(doc) {\n" + "        doc.defaultProject = doc._id + '_personal'\n"
						+ "        db.user.save(doc)   \n" + "    }\n" + ")");
		mongoTemplate.getDb().doEval("db.userPreference.remove({})");
	}
}
