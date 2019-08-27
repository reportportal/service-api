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
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.LogCriteriaConstant.CRITERIA_TEST_ITEM_ID;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class GetTestItemHandlerImplTest {

	private static final String TEST_USER_LOGIN = "test";
	private static final String TEST_PROJECT_NAME = "test_project";

	private static final Long LAUNCH_ID = 1L;
	private static final long PROJECT_ID = 1L;
	private static final long FILTER_ID = 2L;
	private static final int LAUNCHES_LIMIT = 600;

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private GetShareableEntityHandler<UserFilter> getShareableEntityHandler;

	@InjectMocks
	private GetTestItemHandlerImpl handler;

	@Test
	void TestItemNotFound() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		when(testItemRepository.findById(1L)).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser)
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
		when(launchRepository.findById(1L)).thenReturn(Optional.empty());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser)
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
		when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getTestItem(1L, extractProjectDetails(rpUser, "test_project"), rpUser)
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
		when(launchRepository.findById(1L)).thenReturn(Optional.empty());

		final Executable executable = () -> handler.getTestItems(Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder()
						.withSearchCriteria(CRITERIA_TEST_ITEM_ID)
						.withValue("100")
						.withCondition(Condition.EQUALS)
						.build())
				.build(), PageRequest.of(0, 10), extractProjectDetails(rpUser, "test_project"), rpUser, 1L, null, 0);

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
		when(launchRepository.findById(1L)).thenReturn(Optional.of(launch));

		final Executable executable = () -> handler.getTestItems(Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder()
						.withSearchCriteria(CRITERIA_TEST_ITEM_ID)
						.withValue("100")
						.withCondition(Condition.EQUALS)
						.build())
				.build(), PageRequest.of(0, 10), extractProjectDetails(rpUser, "test_project"), rpUser, 1L, null, 0);

		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("Forbidden operation. Specified launch with id '1' not referenced to specified project with id '1'",
				exception.getMessage()
		);
	}

	@Test
	void getTestItemsIdsShouldReturnTestItemsIdsWhenFilterIdIsPresentAndLaunchIdIsPresent() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, TEST_PROJECT_NAME);
		UserFilter userFilter = new UserFilter();
		userFilter.setTargetClass(ObjectType.Launch);
		when(getShareableEntityHandler.getPermitted(FILTER_ID, projectDetails)).thenReturn(userFilter);

		List<Long> expectedTestItemsIds = Arrays.asList(1L, 2L, 3L);
		Page<Long> expectedTestItemsIdsPage = new PageImpl<>(expectedTestItemsIds);
		when(testItemRepository.findIdsByFilter(any(Queryable.class), eq(testItemFilter), any(Pageable.class), eq(testItemPageable)))
				.thenReturn(expectedTestItemsIdsPage);

		//WHEN
		Page<Long> actualTestItemsIdsPage = handler.getTestItemsIds(projectDetails, rpUser, testItemFilter, testItemPageable, LAUNCH_ID,
				FILTER_ID, LAUNCHES_LIMIT);

		//THEN
		assertEquals(expectedTestItemsIdsPage, actualTestItemsIdsPage);
	}

	@Test
	void getTestItemsIdsShouldReturnTestItemsIdsWhenFilterIdIsPresentAndLaunchIdIsNull() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, TEST_PROJECT_NAME);
		UserFilter userFilter = new UserFilter();
		userFilter.setTargetClass(ObjectType.Launch);
		when(getShareableEntityHandler.getPermitted(FILTER_ID, projectDetails)).thenReturn(userFilter);

		List<Long> expectedTestItemsIds = Arrays.asList(1L, 2L, 3L);
		Page<Long> expectedTestItemsIdsPage = new PageImpl<>(expectedTestItemsIds);
		when(testItemRepository.findIdsByFilter(any(Queryable.class), eq(testItemFilter), any(Pageable.class), eq(testItemPageable)))
				.thenReturn(expectedTestItemsIdsPage);

		//WHEN
		Page<Long> actualTestItemsIdsPage = handler.getTestItemsIds(projectDetails, rpUser, testItemFilter, testItemPageable, LAUNCH_ID,
				FILTER_ID, LAUNCHES_LIMIT);

		//THEN
		assertEquals(expectedTestItemsIdsPage, actualTestItemsIdsPage);
	}

	@Test
	void getTestItemsIdsShouldReturnTestItemsIdsWhenFilterIdIsPresentAndLaunchIdIsPresentAndUserRoleIsAdministrator() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.ADMINISTRATOR, ProjectRole.MEMBER, PROJECT_ID);

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, TEST_PROJECT_NAME);
		UserFilter userFilter = new UserFilter();
		userFilter.setTargetClass(ObjectType.Launch);
		when(getShareableEntityHandler.getPermitted(FILTER_ID, projectDetails)).thenReturn(userFilter);

		List<Long> expectedTestItemsIds = Arrays.asList(1L, 2L, 3L);
		Page<Long> expectedTestItemsIdsPage = new PageImpl<>(expectedTestItemsIds);
		when(testItemRepository.findIdsByFilter(any(Queryable.class), eq(testItemFilter), any(Pageable.class), eq(testItemPageable)))
				.thenReturn(expectedTestItemsIdsPage);

		//WHEN
		Page<Long> actualTestItemsIdsPage = handler.getTestItemsIds(projectDetails, rpUser, testItemFilter, testItemPageable, LAUNCH_ID,
				FILTER_ID, LAUNCHES_LIMIT);

		//THEN
		assertEquals(expectedTestItemsIdsPage, actualTestItemsIdsPage);
	}

	@Test
	void getTestItemsIdsShouldThrowExceptionWhenFilterIdIsPresentAndLaunchIdIsPresentAndProjectRoleIsOperator() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.OPERATOR, PROJECT_ID);

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		//WHEN
		final Executable executable = () -> handler.getTestItemsIds(extractProjectDetails(rpUser, TEST_PROJECT_NAME), rpUser,
				testItemFilter, testItemPageable, LAUNCH_ID, FILTER_ID, LAUNCHES_LIMIT);

		//THEN
		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	@Test
	void getTestItemsIdsShouldThrowExceptionWhenFilterIdIsPresentAndLaunchIdIsPresentAndLaunchFilterTargetIsNull() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, TEST_PROJECT_NAME);
		UserFilter userFilter = new UserFilter();
		when(getShareableEntityHandler.getPermitted(FILTER_ID, projectDetails)).thenReturn(userFilter);

		//WHEN
		final Executable executable = () -> handler.getTestItemsIds(projectDetails, rpUser, testItemFilter, testItemPageable, LAUNCH_ID,
				FILTER_ID, LAUNCHES_LIMIT);

		//THEN
		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("Error in handled Request. Please, check specified parameters: 'Incorrect filter target - 'null'. "
				+ "Allowed: 'Launch''", exception.getMessage());
	}

	@Test
	void getTestItemsIdsShouldThrowExceptionWhenFilterIdIsPresentAndLaunchIdIsPresentAndLaunchesLimitIsLessThanZero() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, TEST_PROJECT_NAME);
		UserFilter userFilter = new UserFilter();
		userFilter.setTargetClass(ObjectType.Launch);
		when(getShareableEntityHandler.getPermitted(FILTER_ID, projectDetails)).thenReturn(userFilter);

		//WHEN
		final Executable executable = () -> handler.getTestItemsIds(projectDetails, rpUser, testItemFilter, testItemPageable, LAUNCH_ID,
				FILTER_ID, -5);

		//THEN
		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("Error in handled Request. Please, check specified parameters: 'Launches limit should be greater than 0 "
				+ "and less or equal to 600'", exception.getMessage());
	}

	@Test
	void getTestItemsIdsShouldThrowExceptionWhenFilterIdIsPresentAndLaunchIdIsPresentAndLaunchesLimitIsGreaterThanSixHundred() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, TEST_PROJECT_NAME);
		UserFilter userFilter = new UserFilter();
		userFilter.setTargetClass(ObjectType.Launch);
		when(getShareableEntityHandler.getPermitted(FILTER_ID, projectDetails)).thenReturn(userFilter);

		//WHEN
		final Executable executable = () -> handler.getTestItemsIds(projectDetails, rpUser, testItemFilter, testItemPageable, LAUNCH_ID,
				FILTER_ID, 615);

		//THEN
		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("Error in handled Request. Please, check specified parameters: 'Launches limit should be greater than 0 "
				+ "and less or equal to 600'", exception.getMessage());
	}

	@Test
	void getTestItemsIdsShouldReturnTestItemsIdsWhenFilterIdIsNullAndLaunchIdIsPresent() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(LAUNCH_ID);
		launch.setProjectId(PROJECT_ID);
		item.setLaunchId(launch.getId());
		when(launchRepository.findById(LAUNCH_ID)).thenReturn(Optional.of(launch));

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		List<Long> expectedTestItemsIds = Arrays.asList(1L, 2L, 3L);
		Page<Long> expectedTestItemsIdsPage = new PageImpl<>(expectedTestItemsIds);
		when(testItemRepository.findIdsByFilter(testItemFilter, testItemPageable)).thenReturn(expectedTestItemsIdsPage);

		//WHEN
		Page<Long> actualTestItemsIdsPage = handler.getTestItemsIds(extractProjectDetails(rpUser, TEST_PROJECT_NAME), rpUser,
				testItemFilter, testItemPageable, LAUNCH_ID, null, LAUNCHES_LIMIT
		);

		//THEN
		assertEquals(expectedTestItemsIdsPage, actualTestItemsIdsPage);
	}

	@Test
	void getTestItemsIdsShouldReturnTestItemsIdsWhenFilterIdIsNullAndLaunchIdIsPresentAndUserRoleIsAdministrator() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.ADMINISTRATOR, ProjectRole.MEMBER, PROJECT_ID);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(LAUNCH_ID);
		launch.setProjectId(PROJECT_ID);
		item.setLaunchId(launch.getId());
		when(launchRepository.findById(LAUNCH_ID)).thenReturn(Optional.of(launch));

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		List<Long> expectedTestItemsIds = Arrays.asList(1L, 2L, 3L);
		Page<Long> expectedTestItemsIdsPage = new PageImpl<>(expectedTestItemsIds);
		when(testItemRepository.findIdsByFilter(testItemFilter, testItemPageable)).thenReturn(expectedTestItemsIdsPage);

		//WHEN
		Page<Long> actualTestItemsIdsPage = handler.getTestItemsIds(extractProjectDetails(rpUser, TEST_PROJECT_NAME), rpUser,
				testItemFilter, testItemPageable, LAUNCH_ID, null, LAUNCHES_LIMIT
		);

		//THEN
		assertEquals(expectedTestItemsIdsPage, actualTestItemsIdsPage);
	}

	@Test
	void getTestItemsIdsShouldThrowExceptionWhenFilterIdIsNullAndLaunchIdIsPresentAndLaunchIsNull() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);

		when(launchRepository.findById(LAUNCH_ID)).thenReturn(Optional.empty());

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		//WHEN
		final Executable executable = () -> handler.getTestItemsIds(extractProjectDetails(rpUser, TEST_PROJECT_NAME), rpUser,
				testItemFilter, testItemPageable, LAUNCH_ID, null, LAUNCHES_LIMIT
		);

		//THEN
		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("Launch '1' not found. Did you use correct Launch ID?", exception.getMessage());
	}

	@Test
	void getTestItemsIdsShouldThrowExceptionWhenFilterIdIsNullAndLaunchIdIsPresentAndLaunchProjectIdIsNotEqualToProjectId() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.MEMBER, 2L);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(LAUNCH_ID);
		launch.setProjectId(PROJECT_ID);
		item.setLaunchId(launch.getId());
		when(launchRepository.findById(LAUNCH_ID)).thenReturn(Optional.of(launch));

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		//WHEN
		final Executable executable = () -> handler.getTestItemsIds(extractProjectDetails(rpUser, TEST_PROJECT_NAME), rpUser,
				testItemFilter, testItemPageable, LAUNCH_ID, null, LAUNCHES_LIMIT
		);

		//THEN
		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("Forbidden operation. Specified launch with id '1' not referenced to specified project with id '2'",
				exception.getMessage());
	}

	@Test
	void getTestItemsIdsShouldThrowExceptionWhenFilterIdIsNullAndLaunchIdIsPresentAndProjectRoleIsOperatorAndLaunchModeIsDebug() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.OPERATOR, PROJECT_ID);

		TestItem item = new TestItem();
		Launch launch = new Launch();
		launch.setId(LAUNCH_ID);
		launch.setProjectId(PROJECT_ID);
		launch.setMode(LaunchModeEnum.DEBUG);
		item.setLaunchId(launch.getId());
		when(launchRepository.findById(LAUNCH_ID)).thenReturn(Optional.of(launch));

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		//WHEN
		final Executable executable = () -> handler.getTestItemsIds(extractProjectDetails(rpUser, TEST_PROJECT_NAME), rpUser,
				testItemFilter, testItemPageable, LAUNCH_ID, null, LAUNCHES_LIMIT
		);

		//THEN
		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	@Test
	void getTestItemsIdsShouldThrowExceptionWhenFilterIdIsNullAndLaunchIdIsNull() {
		//GIVEN
		final ReportPortalUser rpUser = getRpUser(TEST_USER_LOGIN, UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);

		Filter testItemFilter = prepareFilter();
		Pageable testItemPageable = preparePageRequest();

		//WHEN
		final Executable executable = () -> handler.getTestItemsIds(extractProjectDetails(rpUser, TEST_PROJECT_NAME), rpUser,
				testItemFilter, testItemPageable, null, null, LAUNCHES_LIMIT);

		//THEN
		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals("Error in handled Request. Please, check specified parameters: 'Neither launch nor filter id specified.'",
				exception.getMessage());
	}

	private Filter prepareFilter() {
		return Filter.builder().withTarget(TestItem.class).withCondition(new FilterCondition(Condition.EQUALS, false,
				String.valueOf(LAUNCH_ID), CRITERIA_LAUNCH_ID)).build();
	}

	private Pageable preparePageRequest() {
		return PageRequest.of(0, 10);
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
		when(launchRepository.findById(2L)).thenReturn(Optional.of(launch));

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getTestItem(1L, extractProjectDetails(operator, "test_project"), operator)
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
				.build(), PageRequest.of(0, 10), extractProjectDetails(rpUser, "test_project"), rpUser, null, 1L, 0);

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
				.build(), PageRequest.of(0, 10), extractProjectDetails(rpUser, "test_project"), rpUser, null, 1L, 0);

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
				.build(), PageRequest.of(0, 10), extractProjectDetails(rpUser, "test_project"), rpUser, null, 1L, 0);

		final ReportPortalException exception = assertThrows(ReportPortalException.class, executable);
		assertEquals(
				"Error in handled Request. Please, check specified parameters: 'Launches limit should be greater than 0 and less or equal to 600'",
				exception.getMessage()
		);
	}
}