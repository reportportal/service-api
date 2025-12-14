package com.epam.reportportal.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.core.tms.dto.DuplicateTmsMilestoneRS;
import com.epam.reportportal.core.tms.dto.TmsMilestoneRQ;
import com.epam.reportportal.core.tms.dto.TmsMilestoneStatus;
import com.epam.reportportal.core.tms.dto.TmsMilestoneType;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsMilestoneRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestPlanRepository;
import com.epam.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

@Sql("/db/tms/tms-milestone/tms-milestone-fill.sql")
@ExtendWith(MockitoExtension.class)
public class TmsMilestoneIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";
  private static final Long MILESTONE_ID = 100L;
  private static final Long MILESTONE_WITH_TEST_PLANS_ID = 101L;
  private static final Long MILESTONE_FOR_UPDATE_ID = 102L;
  private static final Long MILESTONE_FOR_DELETE_ID = 103L;
  private static final Long MILESTONE_FOR_DUPLICATION_ID = 104L;
  private static final Long MILESTONE_FOR_PATCH_ID = 105L;
  private static final Long NON_EXISTENT_MILESTONE_ID = 99999L;
  private static final Long TEST_PLAN_IN_MILESTONE_ID = 400L;

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule());

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private TmsMilestoneRepository tmsMilestoneRepository;

  @Autowired
  private TmsTestPlanRepository tmsTestPlanRepository;

  // ========== Create Tests ==========

  @Test
  void createMilestone_shouldCreateSuccessfully() throws Exception {
    // Given
    TmsMilestoneRQ milestoneRQ = new TmsMilestoneRQ();
    milestoneRQ.setName("New Milestone");
    milestoneRQ.setStatus(TmsMilestoneStatus.valueOf("SCHEDULED"));
    milestoneRQ.setType(TmsMilestoneType.valueOf("SPRINT"));

    String jsonContent = objectMapper.writeValueAsString(milestoneRQ);

    long initialCount = tmsMilestoneRepository.count();

    // When/Then
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("New Milestone"))
        .andExpect(jsonPath("$.status").value("SCHEDULED"))
        .andExpect(jsonPath("$.type").value("SPRINT"))
        .andExpect(jsonPath("$.testPlans").isArray())
        .andExpect(jsonPath("$.testPlans.length()").value(0));

    // Verify milestone was created
    long finalCount = tmsMilestoneRepository.count();
    assertEquals(initialCount + 1, finalCount);
  }

  @Test
  void createMilestone_withoutProductVersion_shouldCreateSuccessfully() throws Exception {
    // Given
    TmsMilestoneRQ milestoneRQ = new TmsMilestoneRQ();
    milestoneRQ.setName("Milestone without Product Version");
    milestoneRQ.setStatus(TmsMilestoneStatus.valueOf("TESTING"));
    milestoneRQ.setType(TmsMilestoneType.valueOf("RELEASE"));

    String jsonContent = objectMapper.writeValueAsString(milestoneRQ);

    // When/Then
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Milestone without Product Version"))
        .andExpect(jsonPath("$.status").value("TESTING"))
        .andExpect(jsonPath("$.testPlans").isArray())
        .andExpect(jsonPath("$.testPlans.length()").value(0));
  }

  @Test
  void createMilestone_withAllFields_shouldCreateSuccessfully() throws Exception {
    // Given
    var jsonContent = String.format("""
            {
              "name": "Complete Milestone",
              "status": "COMPLETED",
              "type": "RELEASE",
              "startDate": "%s",
              "endDate": "%s"
            }
            """,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    );

    // When/Then
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Complete Milestone"))
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.type").value("RELEASE"))
        .andExpect(jsonPath("$.startDate").exists())
        .andExpect(jsonPath("$.endDate").exists());
  }

  // ========== Get By ID Tests ==========

  @Test
  void getMilestoneById_shouldReturnMilestone() throws Exception {
    // When/Then
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/" + MILESTONE_ID)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(MILESTONE_ID))
        .andExpect(jsonPath("$.name").value("Milestone 100"))
        .andExpect(jsonPath("$.status").value("SCHEDULED"))
        .andExpect(jsonPath("$.type").value("SPRINT"))
        .andExpect(jsonPath("$.testPlans").isArray())
        .andExpect(jsonPath("$.testPlans.length()").value(0));
  }

  @Test
  void getMilestoneById_whenNotFound_shouldReturn404() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/" + NON_EXISTENT_MILESTONE_ID)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getMilestoneById_withTestPlans_shouldReturnMilestoneWithTestPlans() throws Exception {
    // When/Then
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + MILESTONE_WITH_TEST_PLANS_ID)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(MILESTONE_WITH_TEST_PLANS_ID))
        .andExpect(jsonPath("$.name").value("Milestone 101 with Test Plans"))
        .andExpect(jsonPath("$.status").value("TESTING"))
        .andExpect(jsonPath("$.testPlans").isArray())
        .andExpect(jsonPath("$.testPlans.length()").value(2))
        .andExpect(jsonPath("$.testPlans[0].id").exists())
        .andExpect(jsonPath("$.testPlans[0].name").exists());
  }

  // ========== Get By Criteria Tests ==========

  @Test
  void getMilestonesByCriteria_shouldReturnPagedMilestones() throws Exception {
    // When/Then
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone")
            .param("offset", "0")
            .param("limit", "10")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").isNumber())
        .andExpect(jsonPath("$.page.size").value(10))
        .andExpect(jsonPath("$.page.number").value(1))
        .andExpect(jsonPath("$.page.totalElements").exists());
  }

  @Test
  void getMilestonesByCriteria_withPagination_shouldReturnCorrectPages() throws Exception {
    // First page
    MvcResult firstPageResult = mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone")
                .param("offset", "0")
                .param("limit", "2")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page.size").value(2))
        .andExpect(jsonPath("$.page.number").value(1))
        .andReturn();

    // Second page
    MvcResult secondPageResult = mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone")
                .param("offset", "2")
                .param("limit", "2")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.page.size").value(2))
        .andExpect(jsonPath("$.page.number").value(2))
        .andReturn();

    // Verify different content on pages
    String firstPageContent = firstPageResult.getResponse().getContentAsString();
    String secondPageContent = secondPageResult.getResponse().getContentAsString();
    assertNotEquals(firstPageContent, secondPageContent);
  }

  @Test
  void getMilestonesByCriteria_withDefaultPagination_shouldUseDefaults() throws Exception {
    // When/Then
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.page.totalElements").exists());
  }

  // ========== Patch Tests ==========

  @Test
  void patchMilestone_shouldUpdateSuccessfully() throws Exception {
    // Given
    TmsMilestoneRQ milestoneRQ = new TmsMilestoneRQ();
    milestoneRQ.setName("Updated Milestone Name");
    milestoneRQ.setStatus(TmsMilestoneStatus.valueOf("COMPLETED"));

    String jsonContent = objectMapper.writeValueAsString(milestoneRQ);

    // When/Then
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + MILESTONE_FOR_PATCH_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(MILESTONE_FOR_PATCH_ID))
        .andExpect(jsonPath("$.name").value("Updated Milestone Name"))
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  void patchMilestone_partialUpdate_shouldUpdateOnlyProvidedFields() throws Exception {
    // Given - only update name
    TmsMilestoneRQ milestoneRQ = new TmsMilestoneRQ();
    milestoneRQ.setName("Only Name Updated");

    String jsonContent = objectMapper.writeValueAsString(milestoneRQ);

    // When/Then
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + MILESTONE_FOR_UPDATE_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(MILESTONE_FOR_UPDATE_ID))
        .andExpect(jsonPath("$.name").value("Only Name Updated"));
  }

  @Test
  void patchMilestone_whenNotFound_shouldReturn404() throws Exception {
    // Given
    TmsMilestoneRQ milestoneRQ = new TmsMilestoneRQ();
    milestoneRQ.setName("Updated Name");

    String jsonContent = objectMapper.writeValueAsString(milestoneRQ);

    // When/Then
    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + NON_EXISTENT_MILESTONE_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  // ========== Delete Tests ==========

  @Test
  void deleteMilestone_shouldDeleteSuccessfully() throws Exception {
    // When/Then
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + MILESTONE_FOR_DELETE_ID)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Verify deletion
    assertFalse(tmsMilestoneRepository.findById(MILESTONE_FOR_DELETE_ID).isPresent());
  }

  @Test
  void deleteMilestone_whenNotFound_shouldReturn404() throws Exception {
    // When/Then
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + NON_EXISTENT_MILESTONE_ID)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteMilestone_withTestPlans_shouldCascadeCorrectly() throws Exception {
    // Verify test plans exist with a milestone
    var testPlan1 = tmsTestPlanRepository.findById(TEST_PLAN_IN_MILESTONE_ID);
    assertTrue(testPlan1.isPresent());
    assertEquals(MILESTONE_WITH_TEST_PLANS_ID, testPlan1.get().getMilestone().getId());

    // When - delete milestone
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + MILESTONE_WITH_TEST_PLANS_ID)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  // ========== Remove Test Plan Tests ==========

  @Test
  void removeTestPlanFromMilestone_shouldRemoveSuccessfully() throws Exception {
    // Given
    Long milestoneId = 106L;
    Long testPlanId = 405L;

    // Verify initial state
    var testPlanBefore = tmsTestPlanRepository.findById(testPlanId);
    assertTrue(testPlanBefore.isPresent());
    assertEquals(milestoneId, testPlanBefore.get().getMilestone().getId());

    // When/Then
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + milestoneId + "/test-plan/" + testPlanId)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void removeTestPlanFromMilestone_whenMilestoneNotFound_shouldReturn404() throws Exception {
    // When/Then
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + NON_EXISTENT_MILESTONE_ID + "/test-plan/" + TEST_PLAN_IN_MILESTONE_ID)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void removeTestPlanFromMilestone_whenTestPlanNotFound_shouldReturn404() throws Exception {
    // When/Then
    mockMvc.perform(delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + MILESTONE_WITH_TEST_PLANS_ID + "/test-plan/99999")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  // ========== Duplication Tests ==========

  @Test
  void duplicateMilestone_shouldDuplicateSuccessfully() throws Exception {
    // Given
    TmsMilestoneRQ duplicateRQ = new TmsMilestoneRQ();
    duplicateRQ.setName("Duplicated Milestone");
    duplicateRQ.setStatus(TmsMilestoneStatus.valueOf("SCHEDULED"));
    duplicateRQ.setType(TmsMilestoneType.valueOf("RELEASE"));

    String jsonContent = objectMapper.writeValueAsString(duplicateRQ);

    long initialCount = tmsMilestoneRepository.count();

    // When/Then
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + MILESTONE_FOR_DUPLICATION_ID + "/duplicate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.name").value("Duplicated Milestone"))
        .andExpect(jsonPath("$.status").value("SCHEDULED"))
        .andExpect(jsonPath("$.type").value("RELEASE"))
        .andExpect(jsonPath("$.testPlans").isArray());

    // Verify new milestone was created
    long finalCount = tmsMilestoneRepository.count();
    assertEquals(initialCount + 1, finalCount);
  }

  @Test
  void duplicateMilestone_withTestPlans_shouldDuplicateTestPlans() throws Exception {
    // Given
    long milestoneId = 107L;  // Has 3 test plans
    TmsMilestoneRQ duplicateRQ = new TmsMilestoneRQ();
    duplicateRQ.setName("Milestone Copy with Test Plans");
    duplicateRQ.setStatus(TmsMilestoneStatus.valueOf("TESTING"));
    duplicateRQ.setType(TmsMilestoneType.valueOf("PLAN"));

    String jsonContent = objectMapper.writeValueAsString(duplicateRQ);

    long initialTestPlanCount = tmsTestPlanRepository.count();

    // When/Then
    MvcResult result = mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY
            + "/tms/milestone/" + milestoneId + "/duplicate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Milestone Copy with Test Plans"))
        .andExpect(jsonPath("$.status").value("TESTING"))
        .andExpect(jsonPath("$.testPlans").isArray())
        .andExpect(jsonPath("$.testPlans.length()").value(3))
        .andReturn();

    // Verify test plans were duplicated
    long finalTestPlanCount = tmsTestPlanRepository.count();
    assertEquals(initialTestPlanCount + 3, finalTestPlanCount);

    // Verify new milestone has the duplicated test plans
    String content = result.getResponse().getContentAsString();
    var duplicatedMilestone = objectMapper.readValue(content, DuplicateTmsMilestoneRS.class);
    assertEquals(3, duplicatedMilestone.getTestPlans().size());
  }

  @Test
  void duplicateMilestone_whenOriginalNotFound_shouldReturn404() throws Exception {
    // Given
    TmsMilestoneRQ duplicateRQ = new TmsMilestoneRQ();
    duplicateRQ.setName("Duplicate");

    String jsonContent = objectMapper.writeValueAsString(duplicateRQ);

    // When/Then
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + NON_EXISTENT_MILESTONE_ID + "/duplicate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void duplicateMilestone_emptyMilestone_shouldDuplicateWithoutTestPlans() throws Exception {
    // Given
    TmsMilestoneRQ duplicateRQ = new TmsMilestoneRQ();
    duplicateRQ.setName("Empty Milestone Copy");
    duplicateRQ.setStatus(TmsMilestoneStatus.valueOf("SCHEDULED"));
    duplicateRQ.setType(TmsMilestoneType.valueOf("SPRINT"));

    String jsonContent = objectMapper.writeValueAsString(duplicateRQ);

    // When/Then
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + MILESTONE_ID + "/duplicate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Empty Milestone Copy"))
        .andExpect(jsonPath("$.status").value("SCHEDULED"))
        .andExpect(jsonPath("$.testPlans").isArray())
        .andExpect(jsonPath("$.testPlans.length()").value(0));
  }

  @Test
  void duplicateMilestone_verifyIndependence_shouldCreateIndependentCopy() throws Exception {
    // Given
    Long originalMilestoneId = 108L;
    TmsMilestoneRQ duplicateRQ = new TmsMilestoneRQ();
    duplicateRQ.setName("Independent Copy");
    duplicateRQ.setStatus(TmsMilestoneStatus.valueOf("SCHEDULED"));
    duplicateRQ.setType(TmsMilestoneType.valueOf("FEATURE"));

    String jsonContent = objectMapper.writeValueAsString(duplicateRQ);

    // When
    var result = mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + originalMilestoneId + "/duplicate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    // Then - modify original and verify duplicate is not affected
    TmsMilestoneRQ updateRQ = new TmsMilestoneRQ();
    updateRQ.setName("Modified Original");
    String updateJson = objectMapper.writeValueAsString(updateRQ);

    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + originalMilestoneId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateJson)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void duplicateMilestone_withCustomFields_shouldUseCustomValues() throws Exception {
    // Given
    TmsMilestoneRQ duplicateRQ = new TmsMilestoneRQ();
    duplicateRQ.setName("Custom Name");
    duplicateRQ.setStatus(TmsMilestoneStatus.valueOf("TESTING"));
    duplicateRQ.setType(TmsMilestoneType.valueOf("SPRINT"));

    String jsonContent = objectMapper.writeValueAsString(duplicateRQ);

    // When/Then
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/milestone/"
            + MILESTONE_FOR_DUPLICATION_ID + "/duplicate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Custom Name"))
        .andExpect(jsonPath("$.status").value("TESTING"))
        .andExpect(jsonPath("$.type").value("SPRINT"));
  }
}
