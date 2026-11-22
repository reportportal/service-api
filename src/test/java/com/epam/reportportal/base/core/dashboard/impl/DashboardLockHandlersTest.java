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

package com.epam.reportportal.base.core.dashboard.impl;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static com.epam.reportportal.base.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.widget.content.remover.WidgetContentRemover;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.DashboardRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.DashboardWidgetRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.Dashboard;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.DashboardWidget;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.DashboardWidgetId;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Position;
import com.epam.reportportal.base.model.Size;
import com.epam.reportportal.base.model.dashboard.AddWidgetRq;
import com.epam.reportportal.base.model.dashboard.DashboardResource;
import com.epam.reportportal.base.model.dashboard.UpdateDashboardRQ;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Lock rule for dashboards and widgets: a locked entity can be changed only by its owner, an organization MANAGER or
 * an instance ADMINISTRATOR.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardLockHandlersTest {

  private static final Long PROJECT_ID = 1L;
  private static final Long DASHBOARD_ID = 10L;
  private static final Long OTHER_DASHBOARD_ID = 11L;
  private static final Long WIDGET_ID = 20L;
  private static final String OWNER = "owner";

  @Mock
  private DashboardRepository dashboardRepository;
  @Mock
  private DashboardWidgetRepository dashboardWidgetRepository;
  @Mock
  private WidgetRepository widgetRepository;
  @Mock
  private WidgetContentRemover widgetContentRemover;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  private UpdateDashboardHandlerImpl updateHandler;
  private DeleteDashboardHandlerImpl deleteHandler;

  @BeforeEach
  void setUp() {
    updateHandler = new UpdateDashboardHandlerImpl(dashboardRepository, widgetContentRemover, eventPublisher,
        dashboardWidgetRepository, widgetRepository);
    deleteHandler = new DeleteDashboardHandlerImpl(dashboardRepository, dashboardWidgetRepository, widgetRepository,
        widgetContentRemover, eventPublisher);
  }

  // ---- unlock / lock ----

  @Test
  void unlockLockedDashboardShouldFailForNonOwnerEditor() {
    dashboard(DASHBOARD_ID, true);
    ReportPortalUser editor = editor();

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> updateHandler.toggleDashboardLock(details(editor), DASHBOARD_ID, false, editor));

    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    assertLockedMessage(ex, "Dashboard");
    verify(dashboardRepository, never()).unlockDashboard(any());
  }

  @Test
  void unlockLockedDashboardShouldSucceedForOwnerManagerAndAdministrator() {
    for (ReportPortalUser user : List.of(owner(), manager(), admin())) {
      dashboard(DASHBOARD_ID, true);
      updateHandler.toggleDashboardLock(details(user), DASHBOARD_ID, false, user);
    }
    verify(dashboardRepository, org.mockito.Mockito.times(3)).unlockDashboard(DASHBOARD_ID);
  }

  @Test
  void lockUnlockedDashboardShouldStillBeAllowedForAnyEditor() {
    dashboard(DASHBOARD_ID, false);
    ReportPortalUser editor = editor();

    updateHandler.toggleDashboardLock(details(editor), DASHBOARD_ID, true, editor);

    verify(dashboardRepository).lockDashboard(DASHBOARD_ID);
  }

  @Test
  void unlockUnlockedDashboardShouldBeNoOpForEditor() {
    dashboard(DASHBOARD_ID, false);
    ReportPortalUser editor = editor();

    updateHandler.toggleDashboardLock(details(editor), DASHBOARD_ID, false, editor);

    verify(dashboardRepository, never()).unlockDashboard(any());
  }

  // ---- update ----

  @Test
  void updateLockedDashboardShouldFailForNonOwnerEditor() {
    dashboard(DASHBOARD_ID, true);
    ReportPortalUser editor = editor();

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> updateHandler.updateDashboard(details(editor), updateRq(), DASHBOARD_ID, editor));

    assertLockedMessage(ex, "Dashboard");
    verify(dashboardRepository, never()).save(any());
  }

  @Test
  void updateLockedDashboardShouldSucceedForOwnerManagerAndAdministrator() {
    for (ReportPortalUser user : List.of(owner(), manager(), admin())) {
      dashboard(DASHBOARD_ID, true);
      updateHandler.updateDashboard(details(user), updateRq(), DASHBOARD_ID, user);
    }
    verify(dashboardRepository, org.mockito.Mockito.times(3)).save(any());
  }

  @Test
  void updateUnlockedDashboardShouldSucceedForEditorAndNotTouchWidgetLocks() {
    dashboard(DASHBOARD_ID, false);
    ReportPortalUser editor = editor();

    updateHandler.updateDashboard(details(editor), updateRq(), DASHBOARD_ID, editor);

    verify(dashboardRepository).save(any());
    verify(dashboardRepository, never()).unlockDashboard(any());
    verify(dashboardRepository, never()).lockDashboard(any());
  }

  // ---- add widget ----

  @Test
  void addWidgetToLockedDashboardShouldFailForNonOwnerEditor() {
    dashboard(DASHBOARD_ID, true);
    ReportPortalUser editor = editor();

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> updateHandler.addWidget(DASHBOARD_ID, details(editor), addWidgetRq(), editor));

    assertLockedMessage(ex, "Dashboard");
    verify(dashboardWidgetRepository, never()).save(any());
  }

  @Test
  void addWidgetToLockedDashboardShouldSucceedForOwnerAndLockTheWidget() {
    dashboard(DASHBOARD_ID, true);
    Widget widget = widget(WIDGET_ID, false, "someone-else");
    when(widgetRepository.findByIdAndProjectId(WIDGET_ID, PROJECT_ID)).thenReturn(Optional.of(widget));
    ReportPortalUser owner = owner();

    updateHandler.addWidget(DASHBOARD_ID, details(owner), addWidgetRq(), owner);

    verify(dashboardWidgetRepository).save(any());
    verify(dashboardRepository).lockDashboard(DASHBOARD_ID);
  }

  @Test
  void addLockedWidgetToUnlockedDashboardShouldSucceedForEditor() {
    dashboard(DASHBOARD_ID, false);
    Widget widget = widget(WIDGET_ID, true, "someone-else");
    when(widgetRepository.findByIdAndProjectId(WIDGET_ID, PROJECT_ID)).thenReturn(Optional.of(widget));
    ReportPortalUser editor = editor();

    updateHandler.addWidget(DASHBOARD_ID, details(editor), addWidgetRq(), editor);

    verify(dashboardWidgetRepository).save(any());
  }

  // ---- remove widget ----

  @Test
  void removeWidgetFromLockedDashboardShouldFailForNonOwnerEditor() {
    Dashboard dashboard = dashboard(DASHBOARD_ID, true);
    placeWidget(dashboard, widget(WIDGET_ID, true, OWNER), true);
    ReportPortalUser editor = editor();

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> updateHandler.removeWidget(WIDGET_ID, DASHBOARD_ID, details(editor), editor));

    assertLockedMessage(ex, "Dashboard");
    verify(widgetRepository, never()).delete(any());
  }

  @Test
  void removeLastPlacementOfStaleLockedWidgetFromUnlockedDashboardShouldSucceedForAnyEditor() {
    // the widget still carries a locked flag, but no locked dashboard holds it
    Dashboard dashboard = dashboard(DASHBOARD_ID, false);
    Widget widget = widget(WIDGET_ID, true, OWNER);
    placeWidget(dashboard, widget, true);
    when(dashboardWidgetRepository.countAllByWidgetId(WIDGET_ID)).thenReturn(1);
    ReportPortalUser editor = editor();

    updateHandler.removeWidget(WIDGET_ID, DASHBOARD_ID, details(editor), editor);

    verify(widgetRepository).delete(widget);
  }

  @Test
  void unlinkLockedSharedWidgetFromUnlockedDashboardShouldSucceedForEditorWithoutDeletingIt() {
    Dashboard dashboard = dashboard(DASHBOARD_ID, false);
    Widget widget = widget(WIDGET_ID, true, OWNER);
    DashboardWidget link = placeWidget(dashboard, widget, false);
    placeWidget(dashboard(OTHER_DASHBOARD_ID, true), widget, false);
    when(dashboardWidgetRepository.countAllByWidgetId(WIDGET_ID)).thenReturn(2);
    ReportPortalUser editor = editor();

    updateHandler.removeWidget(WIDGET_ID, DASHBOARD_ID, details(editor), editor);

    verify(dashboardWidgetRepository).delete(link);
    verify(widgetRepository, never()).delete(any());
    assertEquals(true, widget.getLocked());
  }

  @Test
  void unlinkingWidgetFromItsLastLockedDashboardShouldClearTheStaleFlag() {
    Dashboard locked = dashboard(DASHBOARD_ID, true);
    Dashboard unlocked = dashboard(OTHER_DASHBOARD_ID, false);
    Widget widget = widget(WIDGET_ID, true, "widget-owner");
    DashboardWidget link = placeWidget(locked, widget, false);
    placeWidget(unlocked, widget, true);
    when(dashboardWidgetRepository.countAllByWidgetId(WIDGET_ID)).thenReturn(2);
    ReportPortalUser owner = owner();

    updateHandler.removeWidget(WIDGET_ID, DASHBOARD_ID, details(owner), owner);

    verify(dashboardWidgetRepository).delete(link);
    assertEquals(false, widget.getLocked());
    verify(widgetRepository).save(widget);
  }

  @Test
  void removeLockedWidgetLockedOnlyByThisLockedDashboardShouldSucceedForDashboardOwner() {
    Dashboard dashboard = dashboard(DASHBOARD_ID, true);
    dashboard.setOwner("dashboard-owner");
    Widget widget = widget(WIDGET_ID, true, "widget-owner");
    placeWidget(dashboard, widget, true);
    when(dashboardWidgetRepository.countAllByWidgetId(WIDGET_ID)).thenReturn(1);
    ReportPortalUser dashboardOwner = user("dashboard-owner", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.EDITOR);

    updateHandler.removeWidget(WIDGET_ID, DASHBOARD_ID, details(dashboardOwner), dashboardOwner);

    verify(widgetRepository).delete(widget);
  }

  @Test
  void removeWidgetNotOnDashboardShouldFailWithoutDeletingIt() {
    dashboard(DASHBOARD_ID, false);
    Dashboard other = dashboard(OTHER_DASHBOARD_ID, false);
    Widget widget = widget(WIDGET_ID, false, OWNER);
    placeWidget(other, widget, true);
    ReportPortalUser editor = editor();

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> updateHandler.removeWidget(WIDGET_ID, DASHBOARD_ID, details(editor), editor));

    assertEquals(ErrorType.WIDGET_NOT_FOUND_IN_DASHBOARD, ex.getErrorType());
    verify(widgetRepository, never()).delete(any());
    verify(dashboardWidgetRepository, never()).deleteAll(any());
  }

  // ---- delete dashboard ----

  @Test
  void deleteLockedDashboardShouldFailForNonOwnerEditor() {
    dashboard(DASHBOARD_ID, true);
    ReportPortalUser editor = editor();

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> deleteHandler.deleteDashboard(DASHBOARD_ID, details(editor), editor));

    assertLockedMessage(ex, "Dashboard");
    verify(dashboardRepository, never()).delete(any());
  }

  @Test
  void deleteLockedDashboardShouldSucceedForOwnerManagerAndAdministrator() {
    for (ReportPortalUser user : List.of(owner(), manager(), admin())) {
      dashboard(DASHBOARD_ID, true);
      deleteHandler.deleteDashboard(DASHBOARD_ID, details(user), user);
    }
    verify(dashboardRepository, org.mockito.Mockito.times(3)).delete(any());
  }

  @Test
  void deleteUnlockedDashboardShouldSucceedForEditor() {
    dashboard(DASHBOARD_ID, false);
    ReportPortalUser editor = editor();

    deleteHandler.deleteDashboard(DASHBOARD_ID, details(editor), editor);

    verify(dashboardRepository).delete(any());
  }

  @Test
  void deleteUnlockedDashboardHoldingWidgetLockedByAnotherDashboardShouldBeRefusedAtomically() {
    Dashboard dashboard = dashboard(DASHBOARD_ID, false);
    Dashboard locked = dashboard(OTHER_DASHBOARD_ID, true);
    Widget widget = widget(WIDGET_ID, true, OWNER);
    placeWidget(dashboard, widget, true);
    placeWidget(locked, widget, false);
    ReportPortalUser editor = editor();

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> deleteHandler.deleteDashboard(DASHBOARD_ID, details(editor), editor));

    assertLockedMessage(ex, "Widget");
    verify(widgetContentRemover, never()).removeContent(any());
    verify(dashboardRepository, never()).delete(any());
    verify(widgetRepository, never()).deleteAll(any());
  }

  @Test
  void deleteUnlockedDashboardHoldingWidgetLockedByAnotherDashboardShouldSucceedForThatDashboardOwner() {
    Dashboard dashboard = dashboard(DASHBOARD_ID, false);
    Dashboard locked = dashboard(OTHER_DASHBOARD_ID, true);
    locked.setOwner("b-owner");
    Widget widget = widget(WIDGET_ID, true, "widget-creator");
    placeWidget(dashboard, widget, true);
    placeWidget(locked, widget, false);
    ReportPortalUser owner = user("b-owner", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.EDITOR);

    deleteHandler.deleteDashboard(DASHBOARD_ID, details(owner), owner);

    verify(dashboardRepository).delete(dashboard);
  }

  @Test
  void deleteLockedDashboardHoldingItsOwnLockedWidgetsShouldSucceedForDashboardOwner() {
    Dashboard dashboard = dashboard(DASHBOARD_ID, true);
    dashboard.setOwner("dashboard-owner");
    Widget widget = widget(WIDGET_ID, true, "widget-owner");
    placeWidget(dashboard, widget, true);
    ReportPortalUser dashboardOwner = user("dashboard-owner", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.EDITOR);

    deleteHandler.deleteDashboard(DASHBOARD_ID, details(dashboardOwner), dashboardOwner);

    verify(widgetRepository).deleteAll(List.of(widget));
  }

  @Test
  void deleteUnlockedDashboardHoldingWidgetCreatedByTheUserOnSomeoneElsesLockedDashboardShouldBeRefused() {
    Dashboard dashboard = dashboard(DASHBOARD_ID, false);
    Dashboard locked = dashboard(OTHER_DASHBOARD_ID, true);
    Widget widget = widget(WIDGET_ID, true, "editor");
    placeWidget(dashboard, widget, true);
    placeWidget(locked, widget, false);
    ReportPortalUser creator = editor();

    assertThrows(ReportPortalException.class,
        () -> deleteHandler.deleteDashboard(DASHBOARD_ID, details(creator), creator));
    verify(dashboardRepository, never()).delete(any());
  }

  @Test
  void deleteDashboardWithWidgetOnTwoLockedDashboardsOfDifferentOwnersShouldBeRefusedForEachOwnerButAllowedForManager() {
    Dashboard a = dashboard(DASHBOARD_ID, true);
    a.setOwner("owner-a");
    Dashboard b = dashboard(OTHER_DASHBOARD_ID, true);
    b.setOwner("owner-b");
    Widget widget = widget(WIDGET_ID, true, "widget-creator");
    placeWidget(a, widget, true);
    placeWidget(b, widget, false);
    ReportPortalUser ownerA = user("owner-a", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.EDITOR);

    assertThrows(ReportPortalException.class,
        () -> deleteHandler.deleteDashboard(DASHBOARD_ID, details(ownerA), ownerA));

    ReportPortalUser manager = manager();
    deleteHandler.deleteDashboard(DASHBOARD_ID, details(manager), manager);
    verify(dashboardRepository).delete(a);
  }

  @Test
  void deleteDashboardShouldClearStaleLockOfWidgetsStayingOnOtherDashboards() {
    Dashboard locked = dashboard(DASHBOARD_ID, true);
    Dashboard unlocked = dashboard(OTHER_DASHBOARD_ID, false);
    Widget widget = widget(WIDGET_ID, true, "widget-owner");
    placeWidget(locked, widget, false);
    placeWidget(unlocked, widget, true);
    ReportPortalUser owner = owner();

    deleteHandler.deleteDashboard(DASHBOARD_ID, details(owner), owner);

    assertEquals(false, widget.getLocked());
    verify(widgetRepository).save(widget);
  }

  // ---- helpers ----

  private Dashboard dashboard(Long id, boolean locked) {
    Dashboard dashboard = new Dashboard();
    dashboard.setId(id);
    dashboard.setName("dashboard " + id);
    dashboard.setOwner(OWNER);
    dashboard.setLocked(locked);
    Project project = new Project();
    project.setId(PROJECT_ID);
    dashboard.setProject(project);
    when(dashboardRepository.findByIdAndProjectId(id, PROJECT_ID)).thenReturn(Optional.of(dashboard));
    return dashboard;
  }

  private Widget widget(Long id, boolean locked, String owner) {
    Widget widget = new Widget();
    widget.setId(id);
    widget.setName("widget " + id);
    widget.setOwner(owner);
    widget.setLocked(locked);
    Project project = new Project();
    project.setId(PROJECT_ID);
    widget.setProject(project);
    return widget;
  }

  private DashboardWidget placeWidget(Dashboard dashboard, Widget widget, boolean createdOn) {
    DashboardWidget link = new DashboardWidget();
    link.setId(new DashboardWidgetId(dashboard.getId(), widget.getId()));
    link.setDashboard(dashboard);
    link.setWidget(widget);
    link.setWidgetName(widget.getName());
    link.setCreatedOn(createdOn);
    dashboard.addWidget(link);
    widget.getDashboardWidgets().add(link);
    lenientWidgetLookup(widget);
    return link;
  }

  private void lenientWidgetLookup(Widget widget) {
    org.mockito.Mockito.lenient().when(widgetRepository.findByIdAndProjectId(widget.getId(), PROJECT_ID))
        .thenReturn(Optional.of(widget));
  }

  private AddWidgetRq addWidgetRq() {
    AddWidgetRq rq = new AddWidgetRq();
    rq.setAddWidget(new DashboardResource.WidgetObjectModel("widget " + WIDGET_ID, WIDGET_ID, new Size(5, 5), new Position(0, 0)));
    return rq;
  }

  private UpdateDashboardRQ updateRq() {
    UpdateDashboardRQ rq = new UpdateDashboardRQ();
    rq.setName("dashboard " + DASHBOARD_ID);
    rq.setDescription("updated");
    return rq;
  }

  private void assertLockedMessage(ReportPortalException ex, String entity) {
    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    assertEquals("You do not have enough permissions. " + entity
        + (entity.equals("Widget") ? " is on a locked dashboard. Only that dashboard's owner, an organization manager or an administrator can change it."
        : " is locked. Only its owner, an organization manager or an administrator can change it."), ex.getMessage());
  }

  private ReportPortalUser owner() {
    return user(OWNER, UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER);
  }

  private ReportPortalUser editor() {
    return user("editor", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.EDITOR);
  }

  private ReportPortalUser manager() {
    return user("manager", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.VIEWER);
  }

  private ReportPortalUser admin() {
    return user("admin", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.VIEWER);
  }

  private ReportPortalUser user(String login, UserRole role, OrganizationRole orgRole, ProjectRole projectRole) {
    return getRpUser(login, role, orgRole, projectRole, PROJECT_ID);
  }

  private MembershipDetails details(ReportPortalUser user) {
    return extractProjectDetails(user, "test_project");
  }
}
