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

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.ws.model.TestItemHistoryResource;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.converter.helper.TestItemCreationHelper.prepareTestItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class TestItemConverterTest {

	private static final double DURATION = 0.75355;

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
		//GIVEN
		final TestItem item = prepareTestItem();

		//WHEN
		final TestItemActivityResource activityResource = TestItemConverter.TO_ACTIVITY_RESOURCE.apply(item, 4L);

		//THEN
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
		//GIVEN
		final TestItem testItem = prepareTestItem();

		//WHEN
		final TestItemResource resource = TestItemConverter.TO_RESOURCE.apply(testItem);

		//THEN
		assertTestItemResourceFieldsEqual(testItem, resource);

		assertEquals(testItem.getParent().getItemId(), resource.getParent());

		assertNotNull(resource.getIssue());

		assertThat(resource.getParameters()
				.stream()
				.map(ParametersConverter.TO_MODEL)
				.collect(Collectors.toSet())).containsExactlyElementsOf(testItem.getParameters());
	}

	@Test
	void toHistoryResourceShouldConvertTestItemToTestItemHistoryResource() {
		//GIVEN
		final TestItem testItem = prepareTestItem();
		TestItemResults itemResults = testItem.getItemResults();
		itemResults.setDuration(DURATION);

		//WHEN
		final TestItemHistoryResource historyResource = TestItemConverter.TO_HISTORY_RESOURCE.apply(testItem);

		//THEN
		assertTestItemResourceFieldsEqual(testItem, historyResource);

		assertEquals(testItem.getParent().getItemId(), historyResource.getParent());

		assertNotNull(historyResource.getIssue());

		assertThat(historyResource.getParameters()
				.stream()
				.map(ParametersConverter.TO_MODEL)
				.collect(Collectors.toSet())).containsExactlyElementsOf(testItem.getParameters());

		assertEquals(itemResults.getDuration(), historyResource.getDuration());
	}

	@Test
	void toHistoryResourceShouldConvertTestItemToTestItemHistoryResourceWhenParametersAreNull() {
		//GIVEN
		final TestItem testItem = prepareTestItem();
		TestItemResults itemResults = testItem.getItemResults();
		itemResults.setDuration(DURATION);
		testItem.setParameters(null);

		//WHEN
		final TestItemHistoryResource historyResource = TestItemConverter.TO_HISTORY_RESOURCE.apply(testItem);

		//THEN
		assertTestItemResourceFieldsEqual(testItem, historyResource);

		assertEquals(testItem.getParent().getItemId(), historyResource.getParent());

		assertNotNull(historyResource.getIssue());

		assertNull(testItem.getParameters());

		assertEquals(itemResults.getDuration(), historyResource.getDuration());
	}

	@Test
	void toHistoryResourceShouldConvertTestItemToTestItemHistoryResourceWhenParentIsNull() {
		//GIVEN
		final TestItem testItem = prepareTestItem();
		TestItemResults itemResults = testItem.getItemResults();
		itemResults.setDuration(DURATION);
		testItem.setParent(null);

		//WHEN
		final TestItemHistoryResource historyResource = TestItemConverter.TO_HISTORY_RESOURCE.apply(testItem);

		//THEN
		assertTestItemResourceFieldsEqual(testItem, historyResource);

		assertNull(testItem.getParent());

		assertNotNull(historyResource.getIssue());

		assertThat(historyResource.getParameters()
				.stream()
				.map(ParametersConverter.TO_MODEL)
				.collect(Collectors.toSet())).containsExactlyElementsOf(testItem.getParameters());

		assertEquals(itemResults.getDuration(), historyResource.getDuration());
	}

	@Test
	void toHistoryResourceShouldConvertTestItemToTestItemHistoryResourceWhenIssueIsNull() {
		//GIVEN
		final TestItem testItem = prepareTestItem();
		TestItemResults itemResults = testItem.getItemResults();
		itemResults.setDuration(DURATION);
		itemResults.setIssue(null);

		//WHEN
		final TestItemHistoryResource historyResource = TestItemConverter.TO_HISTORY_RESOURCE.apply(testItem);

		//THEN
		assertTestItemResourceFieldsEqual(testItem, historyResource);

		assertEquals(testItem.getParent().getItemId(), historyResource.getParent());

		assertNull(historyResource.getIssue());

		assertThat(historyResource.getParameters()
				.stream()
				.map(ParametersConverter.TO_MODEL)
				.collect(Collectors.toSet())).containsExactlyElementsOf(testItem.getParameters());

		assertEquals(itemResults.getDuration(), historyResource.getDuration());
	}

	@Test
	void toHistoryResourceShouldConvertTestItemToTestItemHistoryResourceWhenLaucnhIdIsNull() {
		//GIVEN
		final TestItem testItem = prepareTestItem();
		TestItemResults itemResults = testItem.getItemResults();
		itemResults.setDuration(DURATION);

		testItem.setLaunchId(null);

		//WHEN
		final TestItemHistoryResource historyResource = TestItemConverter.TO_HISTORY_RESOURCE.apply(testItem);

		//THEN
		assertTestItemResourceFieldsEqual(testItem, historyResource);

		assertEquals(testItem.getParent().getItemId(), historyResource.getParent());

		assertNotNull(historyResource.getIssue());

		assertNull(historyResource.getLaunchId());

		assertThat(historyResource.getParameters()
				.stream()
				.map(ParametersConverter.TO_MODEL)
				.collect(Collectors.toSet())).containsExactlyElementsOf(testItem.getParameters());

		assertEquals(itemResults.getDuration(), historyResource.getDuration());
	}

	private void assertTestItemResourceFieldsEqual(TestItem testItem, TestItemResource resource) {
		assertEquals(testItem.getName(), resource.getName());
		assertEquals(testItem.getDescription(), resource.getDescription());
		assertEquals(testItem.getLaunchId(), resource.getLaunchId());
		assertEquals(testItem.getUuid(), resource.getUuid());
		assertEquals(testItem.getItemId(), resource.getItemId());
		assertEquals(testItem.getPath(), resource.getPath());
		assertEquals(testItem.getItemResults().getStatus().name(), resource.getStatus());
		assertEquals(testItem.getType().name(), resource.getType());

		assertEquals(Date.from(testItem.getStartTime().atZone(ZoneId.of("UTC")).toInstant()), resource.getStartTime());
		assertEquals(Date.from(testItem.getItemResults().getEndTime().atZone(ZoneId.of("UTC")).toInstant()), resource.getEndTime());

		assertEquals(testItem.getUniqueId(), resource.getUniqueId());

		assertThat(resource.getAttributes()
				.stream()
				.map(ItemAttributeConverter.FROM_RESOURCE)
				.collect(Collectors.toSet())).containsExactlyElementsOf(testItem.getAttributes());

		assertThat(resource.getStatisticsResource()).isEqualToComparingFieldByField(StatisticsConverter.TO_RESOURCE.apply(testItem.getItemResults()
				.getStatistics()));
	}
}