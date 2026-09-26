/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.activity.impl;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_DETAILS;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_OBJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_OBJECT_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_SUBJECT_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.CompositeFilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.dao.ActivityRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import java.util.List;
import org.jooq.Operator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ActivityHandlerImplTest {

  private static final long PROJECT_ID = 1L;
  private static final long TEST_CASE_ID = 2L;

  @Mock
  private ActivityRepository activityRepository;

  @Mock
  private TestItemRepository testItemRepository;

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Test
  void getTestCaseActivitiesExpandsDetailsContainsFilterToDetailsOrSubjectName() {
    var handler = new ActivityHandlerImpl(activityRepository, testItemRepository, launchRepository,
        projectRepository);
    var membershipDetails = MembershipDetails.builder().withProjectId(PROJECT_ID).build();
    var filter = new Filter(Activity.class, List.of(
        new FilterCondition(Condition.CONTAINS, false, "matching value", CRITERIA_DETAILS)
    ));
    var pageable = PageRequest.of(0, 20);

    when(projectRepository.existsById(PROJECT_ID)).thenReturn(true);
    when(activityRepository.findByFilter(any(Queryable.class), any()))
        .thenReturn(Page.empty(pageable));

    handler.getTestCaseActivities(membershipDetails, TEST_CASE_ID, filter, pageable);

    var filterCaptor = ArgumentCaptor.forClass(Queryable.class);
    verify(activityRepository).findByFilter(filterCaptor.capture(), any());

    var repositoryFilter = assertInstanceOf(Filter.class, filterCaptor.getValue());
    assertEquals(4, repositoryFilter.getFilterConditions().size());

    var searchCondition = assertInstanceOf(CompositeFilterCondition.class,
        repositoryFilter.getFilterConditions().get(0));
    assertEquals(Operator.OR, searchCondition.getOperator());
    assertEquals(2, searchCondition.getConditions().size());

    var detailsCondition = assertInstanceOf(FilterCondition.class,
        searchCondition.getConditions().get(0));
    assertEquals(CRITERIA_DETAILS, detailsCondition.getSearchCriteria());
    assertEquals(Condition.CONTAINS, detailsCondition.getCondition());
    assertEquals("matching value", detailsCondition.getValue());

    var subjectNameCondition = assertInstanceOf(FilterCondition.class,
        searchCondition.getConditions().get(1));
    assertEquals(CRITERIA_SUBJECT_NAME, subjectNameCondition.getSearchCriteria());
    assertEquals(Condition.CONTAINS, subjectNameCondition.getCondition());
    assertEquals("matching value", subjectNameCondition.getValue());
    assertEquals(Operator.OR, subjectNameCondition.getOperator());

    assertScopeCondition(repositoryFilter.getFilterConditions().get(1), CRITERIA_OBJECT_ID,
        String.valueOf(TEST_CASE_ID));
    assertScopeCondition(repositoryFilter.getFilterConditions().get(2), CRITERIA_OBJECT_TYPE,
        EventObject.TMS_TEST_CASE.toString());
    assertScopeCondition(repositoryFilter.getFilterConditions().get(3), CRITERIA_PROJECT_ID,
        String.valueOf(PROJECT_ID));
  }

  private void assertScopeCondition(ConvertibleCondition condition, String criteria, String value) {
    var filterCondition = assertInstanceOf(FilterCondition.class, condition);
    assertEquals(criteria, filterCondition.getSearchCriteria());
    assertEquals(value, filterCondition.getValue());
  }
}
