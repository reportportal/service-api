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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.filter.ObjectType;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.commons.querygen.constant.LogCriteriaConstant.CRITERIA_TEST_ITEM_ID;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class GetTestItemHandlerImplTest {

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private LaunchAccessValidator launchAccessValidator;

	@Mock
	private TestItemService testItemService;

	@Mock
	private GetShareableEntityHandler<UserFilter> getShareableEntityHandler;

	@InjectMocks
	private GetTestItemHandlerImpl handler;

	@Test
	void TestItemNotFound() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		when(testItemRepository.findById(1L)).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getTestItem("1", extractProjectDetails(rpUser, "test_project"), rpUser)
		);
		assertEquals("Test Item '1' not found. Did you use correct Test Item ID?", exception.getMessage());
	}

	@Test
	void getTestItemUnderNotExistedLaunch() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(1L);
		item.setLaunchId(launch.getId());
		when(testItemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(testItemService.getEffectiveLaunch(item)).thenReturn(launch);

		doThrow(new ReportPortalException("Launch '1' not found. Did you use correct Launch ID?")).when(launchAccessValidator)
				.validate(launch.getId(), extractProjectDetails(rpUser, "test_project"), rpUser);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getTestItem("1", extractProjectDetails(rpUser, "test_project"), rpUser)
		);
		assertEquals("Launch '1' not found. Did you use correct Launch ID?", exception.getMessage());
	}

	@Test
	void getTestItemFromAnotherProject() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setProjectId(2L);
		item.setLaunchId(launch.getId());
		when(testItemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(testItemService.getEffectiveLaunch(item)).thenReturn(launch);

		doThrow(new ReportPortalException(
				"Forbidden operation. Specified launch with id '1' not referenced to specified project with id '1'")).when(
				launchAccessValidator).validate(launch.getId(), extractProjectDetails(rpUser, "test_project"), rpUser);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getTestItem("1", extractProjectDetails(rpUser, "test_project"), rpUser)
		);
		assertEquals("Forbidden operation. Specified launch with id '1' not referenced to specified project with id '1'",
				exception.getMessage()
		);
	}

	@Test
	void getTestItemsUnderNotExistedLaunch() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(1L);
		item.setLaunchId(launch.getId());
		doThrow(new ReportPortalException("Launch '1' not found. Did you use correct Launch ID?")).when(launchAccessValidator)
				.validate(item.getLaunchId(), extractProjectDetails(rpUser, "test_project"), rpUser);
		final Executable executable = () -> handler.getTestItems(Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder()
						.withSearchCriteria(CRITERIA_TEST_ITEM_ID)
						.withValue("100")
						.withCondition(Condition.EQUALS)
						.build())
				.build(), PageRequest.of(0, 10), extractProjectDetails(rpUser, "test_project"), rpUser, 1L, null, false, 0);

		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("Launch '1' not found. Did you use correct Launch ID?", exception.getMessage());
	}

	@Test
	public void getTestItemUnderAnotherProject() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setProjectId(2L);
		item.setLaunchId(launch.getId());
		doThrow(new ReportPortalException(
				"Forbidden operation. Specified launch with id '1' not referenced to specified project with id '1'")).when(
				launchAccessValidator).validate(item.getLaunchId(), extractProjectDetails(rpUser, "test_project"), rpUser);

		final Executable executable = () -> handler.getTestItems(Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder()
						.withSearchCriteria(CRITERIA_TEST_ITEM_ID)
						.withValue("100")
						.withCondition(Condition.EQUALS)
						.build())
				.build(), PageRequest.of(0, 10), extractProjectDetails(rpUser, "test_project"), rpUser, 1L, null, false, 0);

		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("Forbidden operation. Specified launch with id '1' not referenced to specified project with id '1'",
				exception.getMessage()
		);
	}

	@Test
	void getItemByOperator() {
		ReportPortalUser operator = getRpUser("operator", UserRole.USER, ProjectRole.OPERATOR, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(2L);
		launch.setMode(LaunchModeEnum.DEBUG);
		launch.setProjectId(1L);
		item.setLaunchId(launch.getId());

		when(testItemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(testItemService.getEffectiveLaunch(item)).thenReturn(launch);
		doThrow(new ReportPortalException("You do not have enough permissions.")).when(launchAccessValidator).validate(launch.getId(), extractProjectDetails(operator, "test_project"), operator);

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getTestItem("1", extractProjectDetails(operator, "test_project"), operator)
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	@Test
	public void getItemsForNonExistingFilter() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setProjectId(2L);
		item.setLaunchId(launch.getId());
		when(getShareableEntityHandler.getPermitted(1L, extractProjectDetails(rpUser, "test_project"))).thenThrow(new ReportPortalException(
				ErrorType.USER_FILTER_NOT_FOUND_IN_PROJECT,
				1L,
				"test_project"
		));

		final Executable executable = () -> handler.getTestItems(Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder()
						.withSearchCriteria(CRITERIA_TEST_ITEM_ID)
						.withValue("100")
						.withCondition(Condition.EQUALS)
						.build())
				.build(), PageRequest.of(0, 10), extractProjectDetails(rpUser, "test_project"), rpUser, null, 1L, false, 0);

		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("User filter with ID '1' not found on project 'test_project'. Did you use correct User Filter ID?",
				exception.getMessage()
		);
	}

	@Test
	public void getItemsForIncorrectTargetClass() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setProjectId(2L);
		item.setLaunchId(launch.getId());
		UserFilter filter = new UserFilter();
		filter.setTargetClass(ObjectType.TestItem);
		when(getShareableEntityHandler.getPermitted(1L, extractProjectDetails(rpUser, "test_project"))).thenReturn(filter);

		final Executable executable = () -> handler.getTestItems(Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder()
						.withSearchCriteria(CRITERIA_TEST_ITEM_ID)
						.withValue("100")
						.withCondition(Condition.EQUALS)
						.build())
				.build(), PageRequest.of(0, 10), extractProjectDetails(rpUser, "test_project"), rpUser, null, 1L, false, 0);

		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals(
				"Error in handled Request. Please, check specified parameters: 'Incorrect filter target - 'TestItem'. Allowed: 'Launch''",
				exception.getMessage()
		);
	}

	@Test
	public void getItemsForIncorrectLaunchesLimit() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setProjectId(2L);
		item.setLaunchId(launch.getId());
		UserFilter filter = new UserFilter();
		filter.setTargetClass(ObjectType.Launch);
		when(getShareableEntityHandler.getPermitted(1L, extractProjectDetails(rpUser, "test_project"))).thenReturn(filter);

		final Executable executable = () -> handler.getTestItems(Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder()
						.withSearchCriteria(CRITERIA_TEST_ITEM_ID)
						.withValue("100")
						.withCondition(Condition.EQUALS)
						.build())
				.build(), PageRequest.of(0, 10), extractProjectDetails(rpUser, "test_project"), rpUser, null, 1L, false, 0);

		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals(
				"Error in handled Request. Please, check specified parameters: 'Launches limit should be greater than 0'",
				exception.getMessage()
		);
	}
}