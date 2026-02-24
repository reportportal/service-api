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

package com.epam.ta.reportportal.core.widget.impl;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.widget.content.updater.validator.WidgetValidator;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.model.widget.ContentParameters;
import com.epam.ta.reportportal.model.widget.WidgetRQ;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class UpdateWidgetHandlerImplTest {

  private static final Long WIDGET_ID = 1L;
  private static final Long PROJECT_ID = 1L;
  private static final String WIDGET_NAME = "Test Widget";
  private static final String WIDGET_TYPE = "launchStatistics";

  @Mock
  private WidgetRepository widgetRepository;

  @Mock
  private UserFilterRepository filterRepository;

  @Mock
  private MessageBus messageBus;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private WidgetValidator widgetContentFieldsValidator;

  @InjectMocks
  private UpdateWidgetHandlerImpl handler;

  private Widget lockedWidget;
  private Widget unlockedWidget;

  @BeforeEach
  void setUp() {
    Project project = new Project();
    project.setId(PROJECT_ID);

    lockedWidget = createWidget(true, project);
    unlockedWidget = createWidget(false, project);
  }

  @Test
  void updateLockedWidgetWithFilterOnlyChangeShouldSucceedForMember() throws Exception {
    // given
    ReportPortalUser user = getRpUser("member", UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);
    ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(user, "test_project");

    WidgetRQ request = createFilterOnlyUpdateRequest();

    when(widgetRepository.findByIdAndProjectId(WIDGET_ID, PROJECT_ID))
        .thenReturn(Optional.of(lockedWidget));
    when(filterRepository.findByFilter(any(), any()))
        .thenReturn(new PageImpl<>(List.of()));
    doNothing().when(widgetContentFieldsValidator).validate(any(Widget.class));
    when(widgetRepository.save(any(Widget.class))).thenReturn(lockedWidget);
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    doNothing().when(messageBus).publishActivity(any());

    // when
    OperationCompletionRS result = handler.updateWidget(WIDGET_ID, request, projectDetails, user);

    // then
    assertEquals("Widget with ID = '1' successfully updated.", result.getResultMessage());
    verify(widgetRepository).save(any(Widget.class));
  }

  @Test
  void updateLockedWidgetWithStructuralChangeShouldFailForMember() {
    // given
    ReportPortalUser user = getRpUser("member", UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);
    ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(user, "test_project");

    WidgetRQ request = createStructuralUpdateRequest();

    when(widgetRepository.findByIdAndProjectId(WIDGET_ID, PROJECT_ID))
        .thenReturn(Optional.of(lockedWidget));

    // when & then
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.updateWidget(WIDGET_ID, request, projectDetails, user));

    assertEquals(
        "You do not have enough permissions. Widget is used in a locked dashboard. Action is not permitted for your role.",
        exception.getMessage());
  }

  @Test
  void updateLockedWidgetWithStructuralChangeShouldSucceedForAdmin() throws Exception {
    // given
    ReportPortalUser admin = getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.MEMBER, PROJECT_ID);
    ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(admin, "test_project");

    WidgetRQ request = createStructuralUpdateRequest();

    when(widgetRepository.findByIdAndProjectId(WIDGET_ID, PROJECT_ID))
        .thenReturn(Optional.of(lockedWidget));
    doNothing().when(widgetContentFieldsValidator).validate(any(Widget.class));
    when(widgetRepository.existsByNameAndOwnerAndProjectId(any(), any(), anyLong()))
        .thenReturn(false);
    when(widgetRepository.save(any(Widget.class))).thenReturn(lockedWidget);
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    doNothing().when(messageBus).publishActivity(any());

    // when
    OperationCompletionRS result = handler.updateWidget(WIDGET_ID, request, projectDetails, admin);

    // then
    assertEquals("Widget with ID = '1' successfully updated.", result.getResultMessage());
  }

  @Test
  void updateLockedWidgetWithStructuralChangeShouldSucceedForProjectManager() throws Exception {
    // given
    ReportPortalUser pm = getRpUser("pm", UserRole.USER, ProjectRole.PROJECT_MANAGER, PROJECT_ID);
    ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(pm, "test_project");

    WidgetRQ request = createStructuralUpdateRequest();

    when(widgetRepository.findByIdAndProjectId(WIDGET_ID, PROJECT_ID))
        .thenReturn(Optional.of(lockedWidget));
    doNothing().when(widgetContentFieldsValidator).validate(any(Widget.class));
    when(widgetRepository.existsByNameAndOwnerAndProjectId(any(), any(), anyLong()))
        .thenReturn(false);
    when(widgetRepository.save(any(Widget.class))).thenReturn(lockedWidget);
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    doNothing().when(messageBus).publishActivity(any());

    // when
    OperationCompletionRS result = handler.updateWidget(WIDGET_ID, request, projectDetails, pm);

    // then
    assertEquals("Widget with ID = '1' successfully updated.", result.getResultMessage());
  }

  @Test
  void updateUnlockedWidgetWithStructuralChangeShouldSucceedForMember() throws Exception {
    // given
    ReportPortalUser user = getRpUser("member", UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);
    ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(user, "test_project");

    WidgetRQ request = createStructuralUpdateRequest();

    when(widgetRepository.findByIdAndProjectId(WIDGET_ID, PROJECT_ID))
        .thenReturn(Optional.of(unlockedWidget));
    doNothing().when(widgetContentFieldsValidator).validate(any(Widget.class));
    when(widgetRepository.existsByNameAndOwnerAndProjectId(any(), any(), anyLong()))
        .thenReturn(false);
    when(widgetRepository.save(any(Widget.class))).thenReturn(unlockedWidget);
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    doNothing().when(messageBus).publishActivity(any());

    // when
    OperationCompletionRS result = handler.updateWidget(WIDGET_ID, request, projectDetails, user);

    // then
    assertEquals("Widget with ID = '1' successfully updated.", result.getResultMessage());
  }

  @Test
  void updateWidgetWithNameChangeOnlyOnLockedWidgetShouldFailForMember() {
    // given
    ReportPortalUser user = getRpUser("member", UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);
    ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(user, "test_project");

    WidgetRQ request = createWidgetRequest(WIDGET_NAME, null);
    request.setName("New Widget Name");

    when(widgetRepository.findByIdAndProjectId(WIDGET_ID, PROJECT_ID))
        .thenReturn(Optional.of(lockedWidget));

    // when & then
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.updateWidget(WIDGET_ID, request, projectDetails, user));

    assertEquals(
        "You do not have enough permissions. Widget is used in a locked dashboard. Action is not permitted for your role.",
        exception.getMessage());
  }

  @Test
  void updateWidgetWithItemsCountChangeOnLockedWidgetShouldFailForMember() {
    // given
    ReportPortalUser user = getRpUser("member", UserRole.USER, ProjectRole.MEMBER, PROJECT_ID);
    ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(user, "test_project");

    ContentParameters params = new ContentParameters();
    params.setItemsCount(999);
    params.setContentFields(List.of("field1"));
    Map<String, Object> options = new HashMap<>();
    options.put("key", "value");
    params.setWidgetOptions(options);

    WidgetRQ request = createWidgetRequest(WIDGET_NAME, params);

    when(widgetRepository.findByIdAndProjectId(WIDGET_ID, PROJECT_ID))
        .thenReturn(Optional.of(lockedWidget));

    // when & then
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.updateWidget(WIDGET_ID, request, projectDetails, user));

    assertEquals(
        "You do not have enough permissions. Widget is used in a locked dashboard. Action is not permitted for your role.",
        exception.getMessage());
  }

  private Widget createWidget(boolean locked, Project project) {
    Widget widget = new Widget();
    widget.setId(UpdateWidgetHandlerImplTest.WIDGET_ID);
    widget.setName(UpdateWidgetHandlerImplTest.WIDGET_NAME);
    widget.setWidgetType(UpdateWidgetHandlerImplTest.WIDGET_TYPE);
    widget.setDescription(null);
    widget.setLocked(locked);
    widget.setProject(project);
    widget.setItemsCount(10);
    widget.setContentFields(Sets.newHashSet("field1"));
    Map<String, Object> options = new HashMap<>();
    options.put("key", "value");
    widget.setWidgetOptions(new WidgetOptions(options));
    return widget;
  }

  private WidgetRQ createFilterOnlyUpdateRequest() {
    ContentParameters params = new ContentParameters();
    params.setItemsCount(10);
    params.setContentFields(List.of("field1"));
    Map<String, Object> options = new HashMap<>();
    options.put("key", "value");
    params.setWidgetOptions(options);

    WidgetRQ request = createWidgetRequest(WIDGET_NAME, params);
    request.setFilterIds(List.of(100L, 200L));
    return request;
  }

  private WidgetRQ createStructuralUpdateRequest() {
    ContentParameters params = new ContentParameters();
    params.setItemsCount(10);
    params.setContentFields(List.of("field1"));
    Map<String, Object> options = new HashMap<>();
    options.put("key", "value");
    params.setWidgetOptions(options);

    return createWidgetRequest("New Widget Name", params);
  }

  private WidgetRQ createWidgetRequest(String name, ContentParameters params) {
    WidgetRQ request = new WidgetRQ();
    request.setName(name);
    request.setWidgetType(UpdateWidgetHandlerImplTest.WIDGET_TYPE);
    request.setContentParameters(params);
    return request;
  }
}
