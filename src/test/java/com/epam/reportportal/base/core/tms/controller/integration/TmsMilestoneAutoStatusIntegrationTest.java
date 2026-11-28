package com.epam.reportportal.base.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.tms.service.TmsMilestoneAutoStatusService;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsMilestoneRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneStatus;
import com.epam.reportportal.base.ws.BaseMvcTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

/**
 * Integration tests for the automatic milestone status options (EPMRPP-109759).
 */
@Sql("/db/tms/tms-milestone/tms-milestone-auto-status-fill.sql")
class TmsMilestoneAutoStatusIntegrationTest extends BaseMvcTest {

  private static final String MILESTONE_URL = "/v1/project/superadmin_personal/tms/milestone";

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private TmsMilestoneRepository tmsMilestoneRepository;

  @Autowired
  private TmsMilestoneAutoStatusService tmsMilestoneAutoStatusService;

  private static final Long PROJECT_ID = 1L;

  // findByIdAndProjectId, not findById: the entity maps product_version_id as non-optional, so
  // findById inner-joins it and misses milestones without a product version (service-api#2940).
  private TmsMilestone reload(long milestoneId) {
    entityManager.clear();
    return tmsMilestoneRepository.findByIdAndProjectId(milestoneId, PROJECT_ID).orElseThrow();
  }

  private TmsMilestoneStatus statusOf(long milestoneId) {
    return reload(milestoneId).getStatus();
  }

  @Test
  void applyToAll_shouldMoveOnlyMilestonesWhoseOptionsApply() {
    var changed = tmsMilestoneAutoStatusService.applyToAll(Instant.now());

    assertEquals(3, changed);
    assertEquals(TmsMilestoneStatus.TESTING, statusOf(900));
    assertEquals(TmsMilestoneStatus.SCHEDULED, statusOf(901));
    assertEquals(TmsMilestoneStatus.SCHEDULED, statusOf(902));
    assertEquals(TmsMilestoneStatus.COMPLETED, statusOf(903));
    assertEquals(TmsMilestoneStatus.TESTING, statusOf(904));
    assertEquals(TmsMilestoneStatus.TESTING, statusOf(905));
    assertEquals(TmsMilestoneStatus.SCHEDULED, statusOf(906));
    assertEquals(TmsMilestoneStatus.COMPLETED, statusOf(907));
    assertEquals(TmsMilestoneStatus.TESTING, statusOf(908));
  }

  @Test
  void existsExecutedTestByMilestoneId_shouldIgnoreTestsToRun() {
    assertTrue(tmsMilestoneRepository.existsExecutedTestByMilestoneId(905L));
    assertFalse(tmsMilestoneRepository.existsExecutedTestByMilestoneId(906L));
    assertFalse(tmsMilestoneRepository.existsExecutedTestByMilestoneId(901L));
  }

  @Test
  void onTestExecuted_shouldStartScheduledMilestoneOfLaunch() {
    tmsMilestoneAutoStatusService.onTestExecuted(906L);

    assertEquals(TmsMilestoneStatus.TESTING, statusOf(906));
    assertEquals(TmsMilestoneStatus.SCHEDULED, statusOf(902));
  }

  @Test
  void createMilestone_withOptions_shouldStoreThemAndApplyThem() throws Exception {
    mockMvc.perform(post(MILESTONE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name": "Auto status create", "type": "SPRINT", "status": "SCHEDULED",
                 "startDate": "2020-01-01T00:00:00Z", "endDate": "2099-01-01T00:00:00Z",
                 "autoStartOnStartDate": true, "autoCompleteAfterDeadline": true}
                """)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("TESTING"))
        .andExpect(jsonPath("$.autoStartOnStartDate").value(true))
        .andExpect(jsonPath("$.autoStartOnFirstExecution").value(false))
        .andExpect(jsonPath("$.autoCompleteAfterDeadline").value(true));
  }

  @Test
  void createMilestone_withoutOptions_shouldDefaultToFalseAndKeepStatus() throws Exception {
    mockMvc.perform(post(MILESTONE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name": "Manual status create", "type": "SPRINT", "status": "SCHEDULED",
                 "startDate": "2020-01-01T00:00:00Z", "endDate": "2020-01-31T00:00:00Z"}
                """)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SCHEDULED"))
        .andExpect(jsonPath("$.autoStartOnStartDate").value(false))
        .andExpect(jsonPath("$.autoStartOnFirstExecution").value(false))
        .andExpect(jsonPath("$.autoCompleteAfterDeadline").value(false));
  }

  @Test
  void patchMilestone_withoutOptions_shouldKeepSavedOptions() throws Exception {
    mockMvc.perform(patch(MILESTONE_URL + "/902")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\": \"Renamed\"}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.autoStartOnStartDate").value(true));

    assertTrue(reload(902).isAutoStartOnStartDate());
  }

  @Test
  void patchMilestone_enablingOption_shouldApplyItOnSave() throws Exception {
    mockMvc.perform(patch(MILESTONE_URL + "/901")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\": \"SCHEDULED\", \"autoStartOnStartDate\": true}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("TESTING"))
        .andExpect(jsonPath("$.autoStartOnStartDate").value(true));
  }

  @Test
  void patchMilestone_disablingOption_shouldKeepCurrentStatus() throws Exception {
    mockMvc.perform(patch(MILESTONE_URL + "/900")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"autoStartOnStartDate\": false}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SCHEDULED"))
        .andExpect(jsonPath("$.autoStartOnStartDate").value(false));
  }

  @Test
  void patchMilestone_manualStatusChange_shouldWinOverOptions() throws Exception {
    mockMvc.perform(patch(MILESTONE_URL + "/908")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\": \"SCHEDULED\"}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SCHEDULED"))
        .andExpect(jsonPath("$.autoStartOnStartDate").value(true));
  }

  @Test
  void patchExecution_statusAndComment_shouldStartMilestoneAndSaveComment() throws Exception {
    mockMvc.perform(patch("/v1/project/superadmin_personal/launch/manual/906/test-case/execution/90906")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\": \"IN_PROGRESS\", \"executionComment\": {\"comment\": \"started\"}}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionComment.comment").value("started"));

    assertEquals(TmsMilestoneStatus.TESTING, statusOf(906));
  }
}
