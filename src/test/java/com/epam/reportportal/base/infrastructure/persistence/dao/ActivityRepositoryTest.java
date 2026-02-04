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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_ACTIVITY_ORG_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_CREATED_AT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_OBJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_OBJECT_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_OBJECT_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.dao.ActivityRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.HistoryField;
import com.google.common.collect.Comparators;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

@Sql("/db/fill/activity/activities-fill.sql")
class ActivityRepositoryTest extends BaseMvcTest {

	private static final int ACTIVITIES_COUNT = 8;

	@Autowired
	private ActivityRepository activityRepository;

	//	JPA

	@Test
	@DisplayName("Should find Activity by id")
	void findByIdTest() {
		final Optional<Activity> activityOptional = activityRepository.findById(1L);

		assertTrue(activityOptional.isPresent());
		assertEquals(1L, (long) activityOptional.get().getId());
	}

	@Test
	@DisplayName("Should find all Activities")
	void findAllTest() {
		final List<Activity> activities = activityRepository.findAll();

		assertFalse(activities.isEmpty());
		assertEquals(ACTIVITIES_COUNT, activities.size());
	}

	@Test
	@DisplayName("Should create Activity")
	void createTest() {
		final Activity entity = generateActivity();
		final Activity saved = activityRepository.save(entity);
		entity.setId(saved.getId());
		final List<Activity> all = activityRepository.findAll();

		assertEquals(saved, entity);
		assertEquals(ACTIVITIES_COUNT + 1, all.size());
		assertTrue(all.contains(entity));
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	@Test
	@DisplayName("Should update Activity")
	void updateTest() {
		Activity activity = activityRepository.findById(1L).get();
		final Instant now = Instant.now();
		final ActivityDetails details = generateDetails();
		final EventAction action = EventAction.CREATE;

		activity.setCreatedAt(now);
		activity.setAction(action);
		activity.setDetails(details);

		final Activity updated = activityRepository.save(activity);

		assertEquals(now, updated.getCreatedAt());
		assertThat(updated.getDetails()).isEqualToIgnoringGivenFields(details, "mapper");
		assertEquals(action, updated.getAction());
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	@Test
	@DisplayName("Should delete Activity")
	void deleteTest() {
		final Activity activity = activityRepository.findById(1L).get();
		activityRepository.delete(activity);

		assertEquals(ACTIVITIES_COUNT - 1, activityRepository.findAll().size());
	}

	@Test
	@DisplayName("Should delete Activity by id")
	void deleteById() {
		activityRepository.deleteById(1L);
		assertEquals(ACTIVITIES_COUNT - 1, activityRepository.findAll().size());
	}

	@Test
	@DisplayName("Should check existence of Activity")
	void existsTest() {
		assertTrue(activityRepository.existsById(1L));
		assertFalse(activityRepository.existsById(100L));
		assertTrue(activityRepository.exists(defaultFilter()));
	}

	//	Custom Repositories

	@Test
	void deleteModifiedLaterAgo() {
		Duration period = Duration.ofDays(10);
    Instant bound = Instant.now().minus(period);

		activityRepository.deleteModifiedLaterAgo(1L, period);
		List<Activity> all = activityRepository.findAll();
		all.stream()
        .filter(a -> Objects.nonNull(a.getProjectId()))
        .filter(a -> a.getProjectId().equals(1L))
				.forEach(a -> assertTrue(a.getCreatedAt().isAfter(bound)));
	}

	@Test
	void findByFilterWithSortingAndLimit() {
		List<Activity> activities = activityRepository.findByFilterWithSortingAndLimit(defaultFilter(),
				Sort.by(Sort.Direction.DESC, CRITERIA_CREATED_AT),
				2
		);

		assertEquals(2, activities.size());
		final Instant first = activities.get(0).getCreatedAt();
		final Instant second = activities.get(1).getCreatedAt();
		assertTrue(first.isBefore(second) || first.equals(second));
	}

	@Test
	@DisplayName("Should find Activities by filter")
	void findByFilter() {
		List<Activity> activities = activityRepository.findByFilter(filterById(1));

		assertEquals(1, activities.size());
		assertNotNull(activities.get(0));
	}

	@Test
	void findByFilterPageable() {
		Page<Activity> page = activityRepository.findByFilter(filterById(1), PageRequest.of(0, 10));
		ArrayList<Object> activities = Lists.newArrayList();
		page.forEach(activities::add);

		assertEquals(1, activities.size());
		assertNotNull(activities.get(0));
	}

	@Test
	void findByProjectId() {
		final List<Activity> activities = activityRepository.findByFilter(new Filter(Activity.class,
				Condition.EQUALS,
				false,
				String.valueOf(1),
				CRITERIA_PROJECT_ID
		));
		assertNotNull(activities);
		assertFalse(activities.isEmpty());
		activities.forEach(it -> assertEquals(1L, (long) it.getProjectId()));
	}

	@Test
	void findByEntityType() {
		final List<Activity> activities = activityRepository.findByFilter(new Filter(Activity.class,
				Condition.EQUALS,
				false,
				"LAUNCH",
				CRITERIA_OBJECT_TYPE
		));
		assertNotNull(activities);
		assertFalse(activities.isEmpty());
		activities.forEach(it -> assertEquals(EventObject.LAUNCH,
				it.getObjectType()));
	}

	@Test
	void findByCreationDate() {
    Instant to = Instant.now();
    Instant from = to.minus(7, ChronoUnit.DAYS);
		final List<Activity> activities = activityRepository.findByFilter(new Filter(Activity.class,
				Condition.BETWEEN,
				false,
				from + "," + to,
				CRITERIA_CREATED_AT
		));
		assertNotNull(activities);
		assertFalse(activities.isEmpty());
		activities.forEach(
				it -> assertTrue(it.getCreatedAt().isBefore(to) && it.getCreatedAt().isAfter(from)));
	}

	@Test
	void findByUserLogin() {
		final List<Activity> activities = activityRepository.findByFilter(new Filter(Activity.class,
				Condition.EQUALS,
				false,
				"admin@reportportal.internal",
				CRITERIA_USER
		));
		assertNotNull(activities);
		assertFalse(activities.isEmpty());
		activities.forEach(it -> assertEquals(1L, (long) it.getSubjectId()));
	}

	@Test
	void findByObjectIdTest() {
		final List<Activity> activities = activityRepository.findByFilter(new Filter(Activity.class,
				Condition.EQUALS,
				false,
				"4",
				CRITERIA_OBJECT_ID
		));
		assertNotNull(activities);
		assertFalse(activities.isEmpty());
		activities.forEach(it -> assertEquals(4L, (long) it.getObjectId()));
	}

	@Test
	void objectNameCriteriaTest() {
		String term = "filter";

		List<Activity> activities = activityRepository.findByFilter(Filter.builder()
				.withTarget(Activity.class)
				.withCondition(FilterCondition.builder()
						.withCondition(Condition.CONTAINS)
						.withSearchCriteria(CRITERIA_OBJECT_NAME)
						.withValue(term)
						.build())
				.build());

		assertFalse(activities.isEmpty());
		activities.forEach(it -> assertTrue(it.getObjectName().contains(term)));
	}

  @ParameterizedTest
  @CsvSource(value = {
      "my|7",
      "notmy|0"
  }, delimiter = '|')
    void orgNameCriteriaTest(String term, int expectedAmount) {
    List<Activity> activities = activityRepository.findByFilter(Filter.builder()
        .withTarget(Activity.class)
        .withCondition(FilterCondition.builder()
            .withCondition(Condition.CONTAINS)
            .withSearchCriteria(CRITERIA_ACTIVITY_ORG_NAME)
            .withValue(term)
            .build())
        .build());

    assertEquals(expectedAmount, activities.size());
  }

  @ParameterizedTest
  @CsvSource(value = {
      "eventName|eq|createFilter|2",
      "eventName|ne|createFilter|6",
      "objectType|eq|FILTER|3",
      "objectType|ne|FILTER|5",
      "objectName|eq|filter new test|2",
      "objectName|ne|widget test|7",
      "objectName|eq|nul org activity|1",
      "organizationId|eq|1|7",
      "organizationId|ne|1200|7",
      "organizationName|eq|My organization|7",
      "organizationName|eq|not exists|0",
      "organizationName|ne|My organization|0",
      "organizationName|ne|any|7",
      "projectId|eq|1|3",
      "projectId|eq|2|4",
      "projectId|ne|2|3",
      "projectId|ne|3|7",
      "projectName|eq|default_personal|4",
      "projectName|ne|default_personal|3",
      "subjectType|eq|USER|8",
      "subjectType|ne|USER|0",
      "subjectName|eq|superadmin|3",
      "subjectName|ne|superadmin|5",
      "createdAt|gt|2024-10-12T10:16:47.461972Z|8",
      "createdAt|ne|2024-10-18T10:16:47.461972Z|8",
  }, delimiter = '|')
  void searchActivitiesByFields(String field, String operation, String term, int expectedAmount) {
    List<Activity> activities = activityRepository.findByFilter(Filter.builder()
        .withTarget(Activity.class)
        .withCondition(FilterCondition.builder()
            .withCondition(Condition.findByMarker(operation).get())
            .withSearchCriteria(field)
            .withValue(term)
            .build())
        .build());

    assertEquals(expectedAmount, activities.size());
  }


	private Activity generateActivity() {
		Activity activity = new Activity();
		activity.setAction(EventAction.CREATE);
		activity.setEventName("createDefect");
		activity.setCreatedAt(Instant.now());
		activity.setDetails(new ActivityDetails());
		activity.setObjectId(11L);
		activity.setObjectName("test defect name");
		activity.setObjectType(EventObject.DEFECT_TYPE);
		activity.setPriority(EventPriority.MEDIUM);
		activity.setProjectId(1L);
		activity.setSubjectId(1L);
		activity.setSubjectName("subject_name1");
		activity.setSubjectType(EventSubject.USER);
		return activity;
	}

	private ActivityDetails generateDetails() {
		ActivityDetails details = new ActivityDetails();
		details.setHistory((Arrays.asList(HistoryField.of("test field", "old", "new"),
				HistoryField.of("test field 2", "old", "new"))));
		return details;
	}

	@Test
	void sortingByJoinedColumnTest() {
		PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, CRITERIA_USER));
		Page<Activity> activitiesPage = activityRepository.findByFilter(defaultFilter(), pageRequest);

		assertTrue(Comparators.isInOrder(activitiesPage.getContent(),
				Comparator.comparing(Activity::getSubjectName).reversed()));
	}

	private Filter filterById(long id) {
		return new Filter(Activity.class, Condition.EQUALS, false, String.valueOf(id), "id");
	}

	private Filter defaultFilter() {
		return new Filter(Activity.class, Condition.LOWER_THAN, false, "100", CRITERIA_ID);
	}
}
