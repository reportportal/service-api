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

package com.epam.reportportal.base.ws.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.ReportPortalUserUtil;
import com.epam.reportportal.base.infrastructure.persistence.dao.DashboardRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.OwnedEntityUtils;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.model.Position;
import com.epam.reportportal.base.model.Size;
import com.epam.reportportal.base.model.dashboard.AddWidgetRq;
import com.epam.reportportal.base.model.user.EditUserRQ;
import com.epam.reportportal.base.model.dashboard.DashboardResource;
import com.epam.reportportal.base.model.dashboard.UpdateDashboardRQ;
import com.epam.reportportal.base.model.widget.ContentParameters;
import com.epam.reportportal.base.model.widget.WidgetRQ;
import com.epam.reportportal.base.ws.BaseMvcTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

/**
 * A locked dashboard or widget can be changed, deleted, locked or unlocked only by its owner, an organization MANAGER or
 * an instance ADMINISTRATOR. Fixture: dashboard 13 (locked, owner jaja, project 1) holds locked widgets 5 and 7 (owner
 * jaja); widget 5 is also placed on dashboard 14 (unlocked, owner admin); dashboard 16 is unlocked.
 */
@Sql({"/db/shareable/shareable-fill.sql", "/db/shareable/locks-fill.sql"})
class DashboardWidgetLockControllerTest extends BaseMvcTest {

  private static final String BASE = SUPERADMIN_PROJECT_BASE_URL;
  private static final String LOCKED_DASHBOARD_MESSAGE =
      "You do not have enough permissions. Dashboard is locked. Only its owner, an organization manager or an administrator can change it.";
  private static final String LOCKED_WIDGET_MESSAGE =
      "You do not have enough permissions. Widget is on a locked dashboard. Only that dashboard's owner, an organization manager or an administrator can change it.";

  @Autowired
  private JsonMapper objectMapper;

  @Autowired
  private DashboardRepository dashboardRepository;

  @Autowired
  private WidgetRepository widgetRepository;

  @PersistenceContext
  private EntityManager entityManager;

  private String editor() {
    return oAuthHelper.createAccessToken("editor2@mail.com", "x", UserRole.USER);
  }

  private String owner() {
    return oAuthHelper.createAccessToken("jaja@mail.com", "x", UserRole.USER);
  }

  private String manager() {
    return oAuthHelper.createAccessToken("manager2@mail.com", "x", UserRole.USER);
  }

  private String admin() {
    return oAuthHelper.getSuperadminToken();
  }

  // ---- dashboard: update ----

  @Test
  void updateLockedDashboardIsForbiddenForNonOwnerEditor() throws Exception {
    updateDashboard(13, editor()).andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(LOCKED_DASHBOARD_MESSAGE));
    assertEquals("test admin dashboard", dashboardRepository.findById(13L).orElseThrow().getName());
  }

  @Test
  void updateLockedDashboardIsAllowedForOwnerManagerAndAdministrator() throws Exception {
    updateDashboard(13, owner()).andExpect(status().isOk());
    updateDashboard(13, manager()).andExpect(status().isOk());
    updateDashboard(13, admin()).andExpect(status().isOk());
  }

  @Test
  void updateUnlockedDashboardIsAllowedForNonOwnerEditor() throws Exception {
    updateDashboard(16, editor()).andExpect(status().isOk());
  }

  // ---- dashboard: lock state ----

  @Test
  void unlockLockedDashboardIsForbiddenForNonOwnerEditor() throws Exception {
    patchLock(13, false, editor()).andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(LOCKED_DASHBOARD_MESSAGE));
    assertTrue(dashboardRepository.findById(13L).orElseThrow().getLocked());
  }

  @Test
  void unlockLockedDashboardIsAllowedForOwnerAndUnlocksItsWidgets() throws Exception {
    patchLock(13, false, owner()).andExpect(status().isOk());
    entityManager.flush();
    entityManager.clear();
    assertFalse(dashboardRepository.findById(13L).orElseThrow().getLocked());
    assertFalse(widgetRepository.findById(5L).orElseThrow().getLocked());
    assertFalse(widgetRepository.findById(7L).orElseThrow().getLocked());
  }

  @Test
  void unlockLockedDashboardIsAllowedForManagerAndAdministrator() throws Exception {
    patchLock(13, false, manager()).andExpect(status().isOk());
    patchLock(13, true, admin()).andExpect(status().isOk());
    patchLock(13, false, admin()).andExpect(status().isOk());
  }

  @Test
  void lockUnlockedDashboardIsStillAllowedForNonOwnerEditor() throws Exception {
    patchLock(16, true, editor()).andExpect(status().isOk());
  }

  // ---- dashboard: delete / widgets on it ----

  @Test
  void deleteLockedDashboardIsForbiddenForNonOwnerEditor() throws Exception {
    mockMvc.perform(delete(BASE + "/dashboard/13").with(token(editor())))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(LOCKED_DASHBOARD_MESSAGE));
    assertTrue(dashboardRepository.findById(13L).isPresent());
  }

  @Test
  void deleteLockedDashboardIsAllowedForOwner() throws Exception {
    mockMvc.perform(delete(BASE + "/dashboard/13").with(token(owner()))).andExpect(status().isOk());
  }

  @Test
  void deleteLockedDashboardIsAllowedForManager() throws Exception {
    mockMvc.perform(delete(BASE + "/dashboard/13").with(token(manager()))).andExpect(status().isOk());
  }

  @Test
  void deleteLockedDashboardIsAllowedForAdministrator() throws Exception {
    mockMvc.perform(delete(BASE + "/dashboard/13").with(token(admin()))).andExpect(status().isOk());
  }

  @Test
  void deleteUnlockedDashboardIsAllowedForNonOwnerEditor() throws Exception {
    mockMvc.perform(delete(BASE + "/dashboard/16").with(token(editor()))).andExpect(status().isOk());
  }

  @Test
  void removeWidgetFromLockedDashboardIsForbiddenForNonOwnerEditor() throws Exception {
    mockMvc.perform(delete(BASE + "/dashboard/13/7").with(token(editor())))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(LOCKED_DASHBOARD_MESSAGE));
    assertTrue(widgetRepository.findById(7L).isPresent());
  }

  @Test
  void removeWidgetFromLockedDashboardIsAllowedForOwner() throws Exception {
    mockMvc.perform(delete(BASE + "/dashboard/13/7").with(token(owner()))).andExpect(status().isOk());
  }

  @Test
  void removeLockedWidgetFromUnlockedDashboardUnlinksItForNonOwnerEditorWhenItIsPlacedElsewhere() throws Exception {
    // widget 5 is locked (owner jaja) and placed on locked dashboard 13 and unlocked dashboard 14
    mockMvc.perform(delete(BASE + "/dashboard/14/5").with(token(editor()))).andExpect(status().isOk());
    assertTrue(widgetRepository.findById(5L).isPresent());
  }

  @Test
  void removeWidgetThatIsNotOnTheDashboardIsRejectedAndKeepsTheWidget() throws Exception {
    // widget 9 is not on dashboard 14
    mockMvc.perform(delete(BASE + "/dashboard/14/9").with(token(editor()))).andExpect(status().isNotFound());
    assertTrue(widgetRepository.findById(9L).isPresent());
  }

  @Test
  void addWidgetToLockedDashboardIsForbiddenForNonOwnerEditorAndAllowedForOwner() throws Exception {
    AddWidgetRq rq = new AddWidgetRq();
    rq.setAddWidget(new DashboardResource.WidgetObjectModel("LAUNCH STATISTICS", 6L, new Size(5, 5), new Position(0, 0)));

    mockMvc.perform(put(BASE + "/dashboard/13/add").with(token(editor())).contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isForbidden());
    mockMvc.perform(put(BASE + "/dashboard/13/add").with(token(owner())).contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
  }

  // ---- widget ----

  @Test
  void updateLockedWidgetIsForbiddenForNonOwnerEditor() throws Exception {
    updateWidget(5, editor()).andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(LOCKED_WIDGET_MESSAGE));
    assertEquals("activity stream12", widgetRepository.findById(5L).orElseThrow().getName());
  }

  @Test
  void updateLockedWidgetIsAllowedForOwnerManagerAndAdministrator() throws Exception {
    updateWidget(5, owner()).andExpect(status().isOk());
    updateWidget(5, manager()).andExpect(status().isOk());
    updateWidget(5, admin()).andExpect(status().isOk());
  }

  @Test
  void updateUnlockedWidgetIsAllowedForNonOwnerEditor() throws Exception {
    updateWidget(9, editor()).andExpect(status().isOk());
  }

  // ---- ownership follows a login (email) change ----

  @Test
  void ownerKeepsLockedDashboardAfterAdministratorChangesTheirEmailAndOldLoginDoesNotInheritIt() throws Exception {
    EditUserRQ rq = new EditUserRQ();
    rq.setEmail("jaja-new@mail.com");
    mockMvc.perform(put("/v1/users/jaja@mail.com").with(token(admin())).contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
    entityManager.flush();
    entityManager.clear();

    assertEquals("jaja-new@mail.com", dashboardRepository.findById(13L).orElseThrow().getOwner());
    assertEquals("jaja-new@mail.com", widgetRepository.findById(5L).orElseThrow().getOwner());
    assertEquals("jaja-new@mail.com", widgetRepository.findById(7L).orElseThrow().getOwner());
    assertEquals(0L, ((Number) entityManager.createNativeQuery(
        "SELECT count(*) FROM dashboard_widget WHERE widget_owner = 'jaja@mail.com'").getSingleResult()).longValue());
    // other users' entities are untouched
    assertEquals("admin@reportportal.internal", dashboardRepository.findById(14L).orElseThrow().getOwner());

    String renamedOwner = oAuthHelper.createAccessToken("jaja-new@mail.com", "x", UserRole.USER);
    updateDashboard(13, renamedOwner).andExpect(status().isOk());
    updateWidget(5, renamedOwner).andExpect(status().isOk());
    patchLock(13, false, renamedOwner).andExpect(status().isOk());
    patchLock(13, true, renamedOwner).andExpect(status().isOk());

    // a different account later created with the old address must not inherit the ownership
    // (checked directly: the tokens of the old login are revoked by the change)
    var oldLoginUser = ReportPortalUserUtil.getRpUser("jaja@mail.com", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.EDITOR, 1L);
    var details = new MembershipDetails();
    details.setOrgRole(OrganizationRole.MEMBER);
    details.setProjectId(1L);
    var dashboard = dashboardRepository.findById(13L).orElseThrow();
    assertThrows(ReportPortalException.class,
        () -> OwnedEntityUtils.validateOwnedEntityLocked(dashboard, details, oldLoginUser));
    var newLoginUser = ReportPortalUserUtil.getRpUser("jaja-new@mail.com", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.EDITOR, 1L);
    OwnedEntityUtils.validateOwnedEntityLocked(dashboard, details, newLoginUser);
  }

  // ---- helpers ----

  private ResultActions updateDashboard(long id, String token) throws Exception {
    UpdateDashboardRQ rq = new UpdateDashboardRQ();
    rq.setName("renamed " + id);
    rq.setDescription("updated");
    return mockMvc.perform(put(BASE + "/dashboard/" + id).with(token(token)).contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(rq)));
  }

  private ResultActions patchLock(long id, boolean locked, String token) throws Exception {
    return mockMvc.perform(patch(BASE + "/dashboard/" + id).with(token(token)).contentType(APPLICATION_JSON)
        .content("{ \"locked\": " + locked + " }"));
  }

  private ResultActions updateWidget(long id, String token) throws Exception {
    WidgetRQ rq = new WidgetRQ();
    rq.setName("updated by " + Math.abs(token.hashCode()));
    rq.setDescription("updated");
    rq.setWidgetType(id == 9 ? "launchesDurationChart" : "activityStream");
    ContentParameters params = new ContentParameters();
    params.setContentFields(Arrays.asList("number", "start_time", "user"));
    params.setItemsCount(50);
    rq.setContentParameters(params);
    return mockMvc.perform(put(BASE + "/widget/" + id).with(token(token)).contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(rq)));
  }
}
