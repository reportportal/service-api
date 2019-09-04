/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.helper;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.bts.Ticket;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.statistics.StatisticsField;
import com.google.common.collect.Sets;

import java.time.LocalDateTime;

/**
 * @author <a href="mailto:tatyana_gladysheva@epam.com">Tatyana Gladysheva</a>
 */
public class TestItemCreationHelper {

	public static TestItem prepareTestItem() {
		TestItem item = new TestItem();
		item.setName("name");
		item.setDescription("description");
		item.setStartTime(LocalDateTime.now());
		item.setUniqueId("uniqueId");
		item.setUuid("uuid");
		item.setItemId(1L);
		item.setType(TestItemTypeEnum.STEP);
		item.setPath("1.2.3");

		final Parameter parameter = prepareParameter();
		item.setParameters(Sets.newHashSet(parameter));
		item.setAttributes(Sets.newHashSet(new ItemAttribute("key1", "value1", false), new ItemAttribute("key2", "value2", false)));

		item.setLaunchId(2L);
		item.setHasChildren(false);

		final TestItem parent = new TestItem();
		parent.setItemId(3L);
		item.setParent(parent);

		final TestItemResults itemResults = prepareTestItemResults();
		item.setItemResults(itemResults);
		return item;
	}

	private static Parameter prepareParameter() {
		final Parameter parameter = new Parameter();
		parameter.setKey("key");
		parameter.setValue("value");
		return parameter;
	}

	private static TestItemResults prepareTestItemResults() {
		final TestItemResults itemResults = new TestItemResults();
		itemResults.setStatus(StatusEnum.FAILED);
		itemResults.setEndTime(LocalDateTime.now());

		final IssueEntity issue = new IssueEntity();
		issue.setIssueType(new IssueType(new IssueGroup(TestItemIssueGroup.PRODUCT_BUG), "locator", "long name", "SNA", "color"));
		issue.setIgnoreAnalyzer(false);
		issue.setAutoAnalyzed(false);
		issue.setIssueDescription("issue description");

		final Ticket ticket = new Ticket();
		ticket.setTicketId("ticketId1");
		ticket.setUrl("http:/example.com/ticketId1");

		final Ticket ticket1 = new Ticket();
		ticket1.setTicketId("ticketId2");
		ticket1.setUrl("http:/example.com/ticketId2");
		issue.setTickets(Sets.newHashSet(ticket, ticket1));
		itemResults.setIssue(issue);
		itemResults.setStatistics(Sets.newHashSet(new Statistics(new StatisticsField("statistics$defects$automation_bug$total"), 1, 2L)));
		return itemResults;
	}
}
