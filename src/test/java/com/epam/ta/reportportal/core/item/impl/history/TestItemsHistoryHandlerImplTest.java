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

package com.epam.ta.reportportal.core.item.impl.history;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.GetTestItemHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.ItemPathName;
import com.epam.ta.reportportal.entity.item.PathName;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.converter.TestItemResourceAssembler;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;
import com.epam.ta.reportportal.ws.model.TestItemHistoryResource;
import com.epam.ta.reportportal.ws.model.item.PathNameResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class TestItemsHistoryHandlerImplTest {

	private static final String TEST_USER_LOGIN = "test";
	private static final String TEST_PROJECT_NAME = "test_project";

	private static final Long LAUNCH_ID = 1L;
	private static final Long PROJECT_ID = 1L;
	private static final Long FILTER_ID = 2L;
	private static final Long TEST_ITEM_ID = 1L;

	private static final int LAUNCHES_LIMIT = 600;
	private static final int HISTORY_DEPTH = 5;
	private static final int MIN_HISTORY_DEPTH_BOUND = 0;
	private static final int MAX_HISTORY_DEPTH_BOUND = 31;

	@Mock
	private TestItemRepository testItemRepository;
	@Mock
	private LaunchRepository launchRepository;
	@Mock
	private GetTestItemHandler getTestItemHandler;
	@Mock
	private TestItemResourceAssembler testItemResourceAssembler;
	@InjectMocks
	private TestItemsHistoryHandlerImpl handler;

	@Test
	void getItemsHistoryShouldReturnItemsHistory() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);
		Filter filter = prepareFilter();
		Pageable pageable = preparePageRequest();
		ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, TEST_PROJECT_NAME);

		Launch launch = prepareLaunch();
		Map<Long, PathName> pathNamesMapping = getPathNamesMapping();
		TestItemHistoryResource historyResource = prepareHistoryResource(pathNamesMapping);

		prepareMocks(projectDetails, rpUser, filter, pageable, launch, historyResource, pathNamesMapping);

		//WHEN
		List<TestItemHistoryElement> testItemsHistoryElements = handler.getItemsHistory(projectDetails, rpUser, filter, pageable, LAUNCH_ID,
				FILTER_ID, LAUNCHES_LIMIT, HISTORY_DEPTH);

		//THEN
		assertEquals(1, testItemsHistoryElements.size());

		TestItemHistoryElement testItemHistoryElement = testItemsHistoryElements.get(0);
		assertEquals(LAUNCH_ID, testItemHistoryElement.getLaunchId());
		assertEquals(String.valueOf(launch.getNumber()), testItemHistoryElement.getLaunchNumber());
		assertEquals(launch.getStatus().name(), testItemHistoryElement.getLaunchStatus());
		assertEquals(launch.getStartTime().toString(), testItemHistoryElement.getStartTime());

		List<TestItemHistoryResource> resources = testItemHistoryElement.getResources();
		assertEquals(1, resources.size());
		assertEquals(historyResource, resources.get(0));
	}

	private Filter prepareFilter() {
		return Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(new FilterCondition(Condition.EQUALS, false, String.valueOf(LAUNCH_ID), CRITERIA_LAUNCH_ID))
				.build();
	}

	private Pageable preparePageRequest() {
		return PageRequest.of(0, 10);
	}

	private Launch prepareLaunch() {
		Launch launch = new Launch();
		launch.setId(LAUNCH_ID);
		launch.setNumber(1L);
		launch.setName("Launch 2");
		launch.setStatus(StatusEnum.PASSED);
		launch.setStartTime(LocalDateTime.now());
		launch.setProjectId(PROJECT_ID);
		return launch;
	}

	private Map<Long, PathName> getPathNamesMapping() {
		PathName pathName = new PathName();
		List<ItemPathName> itemPaths = new ArrayList<>();
		ItemPathName itemPathName = new ItemPathName();
		itemPathName.setId(1L);
		itemPathName.setName("Launch 1 Suite");
		pathName.setItemPaths(itemPaths);
		return new HashMap<Long, PathName>() {{
			put(TEST_ITEM_ID, pathName);
		}};
	}

	private TestItemHistoryResource prepareHistoryResource(Map<Long, PathName> pathNamesMapping) {
		TestItemHistoryResource historyResource = new TestItemHistoryResource();
		historyResource.setItemId(TEST_ITEM_ID);

		PathName pathName = pathNamesMapping.get(TEST_ITEM_ID);
		PathNameResource pathNameResource = new PathNameResource();

		pathNameResource.setItemPaths(pathName.getItemPaths().stream()
				.map(path -> new com.epam.ta.reportportal.ws.model.item.ItemPathName(path.getId(), path.getName()))
				.collect(Collectors.toList()));

		historyResource.setPathNames(pathNameResource);
		return historyResource;
	}

	private void prepareMocks(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser rpUser, Queryable filter,
			Pageable pageable, Launch launch, TestItemHistoryResource historyResource, Map<Long, PathName> pathNamesMapping) {

		List<TestItem> itemsForHistory = new ArrayList<>();
		TestItem testItem = prepareTestItem();
		itemsForHistory.add(testItem);
		Page<TestItem> testItemsPage = new PageImpl<>(itemsForHistory);
		when(getTestItemHandler.getTestItemsPage(projectDetails, rpUser, filter, pageable, LAUNCH_ID, FILTER_ID, LAUNCHES_LIMIT))
				.thenReturn(testItemsPage);

		List<Launch> launches = new ArrayList<>();
		launches.add(launch);
		when(launchRepository.findAllById(itemsForHistory.stream().map(TestItem::getLaunchId).collect(Collectors.toSet()))).thenReturn(
				launches);

		when(launchRepository.findById(LAUNCH_ID)).thenReturn(Optional.of(launch));
		List<Launch> launchesHistory = new ArrayList<>();
		launchesHistory.add(launch);
		when(launchRepository.findLaunchesHistory(HISTORY_DEPTH, launch.getId(), launch.getName(), PROJECT_ID)).thenReturn(launchesHistory);

		List<TestItem> itemsHistory = new ArrayList<>();
		itemsHistory.add(testItem);
		when(testItemRepository.loadItemsHistory(itemsForHistory.stream().map(TestItem::getUniqueId).collect(Collectors.toList()),
				launchesHistory.stream().map(Launch::getId).collect(toList()))).thenReturn(itemsHistory);

		when(testItemRepository.selectPathNames(itemsForHistory.stream().map(TestItem::getItemId).collect(toList())))
				.thenReturn(pathNamesMapping);
		when(testItemResourceAssembler.toHistoryResource(testItem, pathNamesMapping.get(TEST_ITEM_ID))).thenReturn(historyResource);
	}

	private TestItem prepareTestItem() {
		TestItem testItem = new TestItem();
		testItem.setItemId(TEST_ITEM_ID);
		testItem.setLaunchId(LAUNCH_ID);
		return testItem;
	}

	@Test
	void getItemsHistoryShouldThrowExceptionWhenHistoryDepthIsLowerThanBound() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);
		Filter filter = prepareFilter();
		Pageable pageable = preparePageRequest();
		ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, TEST_PROJECT_NAME);

		//WHEN
		final Executable executable = () -> handler.getItemsHistory(projectDetails, rpUser, filter, pageable, LAUNCH_ID, FILTER_ID,
				LAUNCHES_LIMIT, MIN_HISTORY_DEPTH_BOUND);

		//THEN
		final com.epam.ta.reportportal.exception.ReportPortalException exception = assertThrows(
				com.epam.ta.reportportal.exception.ReportPortalException.class,
				executable
		);
		assertEquals("Unable to load test item history. Items history depth should be greater than '0' and lower than '31'",
				exception.getMessage()
		);
	}

	@Test
	void getItemsHistoryShouldThrowExceptionWhenHistoryDepthIsGreaterThanBound() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);
		Filter filter = prepareFilter();
		Pageable pageable = preparePageRequest();
		ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, TEST_PROJECT_NAME);

		//WHEN
		final Executable executable = () -> handler.getItemsHistory(projectDetails, rpUser, filter, pageable, LAUNCH_ID, FILTER_ID,
				LAUNCHES_LIMIT, MAX_HISTORY_DEPTH_BOUND);

		//THEN
		final com.epam.ta.reportportal.exception.ReportPortalException exception = assertThrows(
				com.epam.ta.reportportal.exception.ReportPortalException.class,
				executable
		);
		assertEquals("Unable to load test item history. Items history depth should be greater than '0' and lower than '31'",
				exception.getMessage()
		);
	}
}