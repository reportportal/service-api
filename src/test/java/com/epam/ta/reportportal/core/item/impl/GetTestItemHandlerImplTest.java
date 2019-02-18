/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.commons.querygen.constant.LogCriteriaConstant.CRITERIA_TEST_ITEM_ID;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class GetTestItemHandlerImplTest {

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private LaunchRepository launchRepository;

	@InjectMocks
	private GetTestItemHandlerImpl handler;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void TestItemNotFound() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Test Item '1' not found. Did you use correct Test Item ID?");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		when(testItemRepository.findById(1L)).thenReturn(Optional.empty());

		handler.getTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser);
	}

	@Test
	public void getTestItemUnderNotExistedLaunch() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Launch '1' not found. Did you use correct Launch ID?");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(1L);
		item.setLaunch(launch);
		when(testItemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(launchRepository.findById(1L)).thenReturn(Optional.empty());

		handler.getTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser);
	}

	@Test
	public void getTestItemFromAnotherProject() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Forbidden operation. Specified launch with id '1' not referenced to specified project with id '1'");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setProjectId(2L);
		item.setLaunch(launch);
		when(testItemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));

		handler.getTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser);
	}

	@Test
	public void getTestItemsUnderNotExistedLaunch() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Launch '1' not found. Did you use correct Launch ID?");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(1L);
		item.setLaunch(launch);
		when(testItemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(launchRepository.findById(1L)).thenReturn(Optional.empty());

		handler.getTestItems(Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder()
						.withSearchCriteria(CRITERIA_TEST_ITEM_ID)
						.withValue("100")
						.withCondition(Condition.EQUALS)
						.build())
				.build(), PageRequest.of(0, 10), extractProjectDetails(rpUser, "test_project"), rpUser, 1L);
	}

	@Test
	public void getTestItemUnderAnotherProject() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Forbidden operation. Specified launch with id '1' not referenced to specified project with id '1'");

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setProjectId(2L);
		item.setLaunch(launch);
		when(testItemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));

		handler.getTestItems(Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder()
						.withSearchCriteria(CRITERIA_TEST_ITEM_ID)
						.withValue("100")
						.withCondition(Condition.EQUALS)
						.build())
				.build(), PageRequest.of(0, 10), extractProjectDetails(rpUser, "test_project"), rpUser, 1L);
	}
}