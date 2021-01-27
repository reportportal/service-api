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

package com.epam.ta.reportportal.ws.converter.converters;

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
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.statistics.StatisticsField;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class TestItemConverterTest {

	@Test
	void toActivityResourceNullTest() {
		assertThrows(NullPointerException.class, () -> TestItemConverter.TO_ACTIVITY_RESOURCE.apply(null, null));
	}

	@Test
	void toResourceNullTest() {
		assertThrows(NullPointerException.class, () -> TestItemConverter.TO_RESOURCE.apply(null));
	}

	@Test
	void toActivityResource() {
		final TestItem item = getItem(true);
		final TestItemActivityResource activityResource = TestItemConverter.TO_ACTIVITY_RESOURCE.apply(item, 4L);

		assertEquals(activityResource.getId(), item.getItemId());
		assertEquals(activityResource.getName(), item.getName());
		assertEquals((long) activityResource.getProjectId(), 4L);
		assertEquals(activityResource.getIssueDescription(), item.getItemResults().getIssue().getIssueDescription());
		assertEquals(activityResource.getIssueTypeLongName(), item.getItemResults().getIssue().getIssueType().getLongName());
		assertEquals(activityResource.getStatus(), item.getItemResults().getStatus().name());
		assertEquals(
				activityResource.getTickets(),
				item.getItemResults()
						.getIssue()
						.getTickets()
						.stream()
						.map(it -> it.getTicketId().concat(":").concat(it.getUrl()))
						.collect(Collectors.joining(", "))
		);
		assertEquals(activityResource.isIgnoreAnalyzer(), item.getItemResults().getIssue().getIgnoreAnalyzer());
		assertEquals(activityResource.isAutoAnalyzed(), item.getItemResults().getIssue().getAutoAnalyzed());
	}

	@Test
	void toResource() {
		final TestItem item = getItem(true);
		final TestItemResource resource = TestItemConverter.TO_RESOURCE.apply(item);

		assertEquals(resource.getName(), item.getName());
		assertEquals(resource.getDescription(), item.getDescription());
		assertEquals(resource.getLaunchId(), item.getLaunchId());
		assertEquals(resource.getUuid(), item.getUuid());
		assertEquals(resource.getItemId(), item.getItemId());
		assertEquals(resource.getParent(), item.getParentId());
		assertEquals(resource.getPath(), item.getPath());
		assertEquals(resource.getStatus(), item.getItemResults().getStatus().name());
		assertEquals(resource.getType(), item.getType().name());
		assertEquals(resource.getStartTime(), Date.from(item.getStartTime().atZone(ZoneId.of("UTC")).toInstant()));
		assertEquals(resource.getEndTime(), Date.from(item.getItemResults().getEndTime().atZone(ZoneId.of("UTC")).toInstant()));
		assertEquals(resource.getUniqueId(), item.getUniqueId());
		assertThat(resource.getAttributes()
				.stream()
				.map(ItemAttributeConverter.FROM_RESOURCE)
				.collect(Collectors.toSet())).containsExactlyElementsOf(item.getAttributes());
		assertThat(resource.getParameters()
				.stream()
				.map(ParametersConverter.TO_MODEL)
				.collect(Collectors.toSet())).containsExactlyElementsOf(item.getParameters());
		assertThat(resource.getStatisticsResource()).isEqualToComparingFieldByField(StatisticsConverter.TO_RESOURCE.apply(item.getItemResults()
				.getStatistics()));
		assertEquals(resource.getIssue().getComment(), item.getItemResults().getIssue().getIssueDescription());
		assertEquals(resource.getIssue().getAutoAnalyzed(), item.getItemResults().getIssue().getAutoAnalyzed());
		assertEquals(resource.getIssue().getIssueType(), item.getItemResults().getIssue().getIssueType().getLocator());
		assertEquals(resource.getIssue().getIgnoreAnalyzer(), item.getItemResults().getIssue().getIgnoreAnalyzer());
	}

	@Test
	void toResourceWithoutIssue() {
		final TestItem item = getItem(false);
		final TestItemResource resource = TestItemConverter.TO_RESOURCE.apply(item);

		assertEquals(resource.getName(), item.getName());
		assertEquals(resource.getDescription(), item.getDescription());
		assertEquals(resource.getLaunchId(), item.getLaunchId());
		assertEquals(resource.getUuid(), item.getUuid());
		assertEquals(resource.getItemId(), item.getItemId());
		assertEquals(resource.getParent(), item.getParentId());
		assertEquals(resource.getPath(), item.getPath());
		assertEquals(resource.getStatus(), item.getItemResults().getStatus().name());
		assertEquals(resource.getType(), item.getType().name());
		assertEquals(resource.getStartTime(), Date.from(item.getStartTime().atZone(ZoneId.of("UTC")).toInstant()));
		assertEquals(resource.getEndTime(), Date.from(item.getItemResults().getEndTime().atZone(ZoneId.of("UTC")).toInstant()));
		assertEquals(resource.getUniqueId(), item.getUniqueId());
		assertThat(resource.getAttributes()
				.stream()
				.map(ItemAttributeConverter.FROM_RESOURCE)
				.collect(Collectors.toSet())).containsExactlyElementsOf(item.getAttributes());
		assertThat(resource.getParameters()
				.stream()
				.map(ParametersConverter.TO_MODEL)
				.collect(Collectors.toSet())).containsExactlyElementsOf(item.getParameters());
		assertThat(resource.getStatisticsResource()).isEqualToComparingFieldByField(StatisticsConverter.TO_RESOURCE.apply(item.getItemResults()
				.getStatistics()));
		assertNull(resource.getIssue());
	}

	private TestItem getItem(boolean hasIssue) {
		TestItem item = new TestItem();
		item.setName("name");
		item.setDescription("description");
		item.setStartTime(LocalDateTime.now());
		item.setUniqueId("uniqueId");
		item.setUuid("uuid");
		item.setItemId(1L);
		item.setType(TestItemTypeEnum.STEP);
		item.setPath("1.2.3");
		final Parameter parameter = new Parameter();
		parameter.setKey("key");
		parameter.setValue("value");
		item.setParameters(Sets.newHashSet(parameter));
		item.setAttributes(Sets.newHashSet(new ItemAttribute("key1", "value1", false), new ItemAttribute("key2", "value2", false)));
		final Launch launch = new Launch();
		launch.setProjectId(4L);
		launch.setId(2L);
		item.setLaunchId(launch.getId());
		item.setHasChildren(false);
		final TestItem parent = new TestItem();
		parent.setItemId(3L);
		item.setParentId(parent.getItemId());
		final TestItemResults itemResults = new TestItemResults();
		itemResults.setStatus(StatusEnum.FAILED);
		itemResults.setEndTime(LocalDateTime.now());
		if (hasIssue) {
			final IssueEntity issue = new IssueEntity();
			issue.setIssueId(3L);
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
		}
		itemResults.setStatistics(Sets.newHashSet(new Statistics(new StatisticsField("statistics$defects$automation_bug$total"), 1, 2L)));
		item.setItemResults(itemResults);
		return item;
	}

}