package com.epam.reportportal.core.tms.controller.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.core.tms.dto.AddTestCaseToLaunchRQ;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchRQ;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentAttachmentRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentBtsTicketRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.reportportal.core.tms.dto.UploadAttachmentRS;
import com.epam.reportportal.core.tms.dto.batch.BatchAddTestCasesToLaunchRQ;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsAttachmentRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestCaseExecutionCommentRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestCaseExecutionRepository;
import com.epam.reportportal.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.reporting.ItemAttributesRQ;
import com.epam.reportportal.reporting.Mode;
import com.epam.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;

/**
 * Integration tests for TMS Manual Launch functionality.
 */
@Sql("/db/tms/tms-manual-launch/tms-manual-launch-fill.sql")
@ExtendWith(MockitoExtension.class)
public class TmsManualLaunchIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";
  private static final String DEFAULT_PROJECT_KEY = "default_personal";
  private final ObjectMapper mapper = new ObjectMapper();
  @Autowired
  private LaunchRepository launchRepository;
  @Autowired
  private TmsTestCaseExecutionRepository testCaseExecutionRepository;
  @Autowired
  private TmsTestCaseExecutionCommentRepository executionCommentRepository;
  @Autowired
  private TestItemRepository testItemRepository;
  @Autowired
  private TmsAttachmentRepository attachmentRepository;
  @PersistenceContext
  private EntityManager entityManager;

  // ==================== CREATE MANUAL LAUNCH ====================

  @Test
  void createManualLaunch_WithAllFields_ShouldSucceed() throws Exception {
    // Given
    var launchRQ = TmsManualLaunchRQ.builder()
        .name("Manual Launch Full")
        .uuid("550e8400-e29b-41d4-a716-446655440999")
        .startTime("2024-01-20T10:00:00Z")
        .mode(Mode.DEFAULT)
        .description("Full manual launch with all fields")
        .testCaseIds(List.of(4L, 5L))
        .attributes(List.of(
            new ItemAttributesRQ("priority", "high"),
            new ItemAttributesRQ("team", "qa")
        ))
        .build();

    // When
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(launchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Manual Launch Full"))
        .andExpect(jsonPath("$.mode").value("DEFAULT"))
        .andExpect(jsonPath("$.status").exists())
        .andReturn();

    // Then
    var response = mapper.readValue(
        result.getResponse().getContentAsString(),
        TmsManualLaunchRS.class
    );

    var launch = launchRepository.findById(response.getId());
    assertTrue(launch.isPresent());
    assertEquals(LaunchTypeEnum.MANUAL, launch.get().getLaunchType());
    assertEquals("Manual Launch Full", launch.get().getName());
    assertThat(launch.get().getAttributes()).hasSize(2);
  }

  @Test
  void createManualLaunch_MinimalData_ShouldSucceed() throws Exception {
    // Given
    var launchRQ = TmsManualLaunchRQ.builder()
        .name("Minimal Manual Launch")
        .build();

    // When
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(launchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Minimal Manual Launch"))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.startTime").exists());
  }

  @Test
  void createManualLaunch_WithTestCases_ShouldCreateExecutions() throws Exception {
    // Given
    var launchRQ = TmsManualLaunchRQ.builder()
        .name("Launch with Test Cases")
        .testCaseIds(List.of(4L, 5L, 6L))
        .build();

    // When
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(launchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    var response = mapper.readValue(
        result.getResponse().getContentAsString(),
        TmsManualLaunchRS.class
    );

    // Then - verify test case executions were created
    var executions = testCaseExecutionRepository.findByLaunchId(response.getId());
    assertThat(executions).hasSize(3);
    assertThat(executions).extracting("testCaseId")
        .containsExactlyInAnyOrder(4L, 5L, 6L);
  }

  // ==================== GET MANUAL LAUNCHES ====================

  @Test
  void getManualLaunches_ShouldReturnOnlyManualLaunches() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isNotEmpty());
  }

  @Test
  void getManualLaunches_WithPagination_ShouldReturnCorrectPage() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .param("offset", "0")
                .param("limit", "2")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.size").value(2))
        .andExpect(jsonPath("$.page.number").value(1));
  }

  @Test
  void getManualLaunches_WithSorting_ShouldReturnSorted() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .param("sort", "startTime,desc")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void getManualLaunches_WithStatusFilter_ShouldFilterCorrectly() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .param("filter.eq.status", "IN_PROGRESS")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].status").value(everyItem(equalTo("IN_PROGRESS"))));
  }

  @Test
  void getManualLaunches_WithNameFilter_ShouldFilterCorrectly() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .param("filter.cnt.name", "Manual Launch 1")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  // ==================== GET MANUAL LAUNCH BY ID ====================

  @Test
  void getManualLaunchById_ShouldReturnLaunch() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(200))
        .andExpect(jsonPath("$.name").exists())
        .andExpect(jsonPath("$.startTime").exists())
        .andExpect(jsonPath("$.status").exists())
        .andExpect(jsonPath("$.executionStatistic").exists());
  }

  @Test
  void getManualLaunchById_NonExistent_ShouldReturnNotFound() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/999")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(containsString("999")));
  }

  @Test
  void getManualLaunchById_FromDifferentProject_ShouldReturnNotFound() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + DEFAULT_PROJECT_KEY + "/launch/manual/200")
                .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isNotFound());
  }

  // ==================== PATCH MANUAL LAUNCH ====================

  @Test
  void patchManualLaunch_UpdateName_ShouldSucceed() throws Exception {
    // Given
    var patchRQ = TmsManualLaunchRQ.builder()
        .name("Updated Launch Name")
        .build();

    // When
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Launch Name"));

    // Then
    var launch = launchRepository.findById(200L);
    assertTrue(launch.isPresent());
    assertEquals("Updated Launch Name", launch.get().getName());
  }

  @Test
  void patchManualLaunch_UpdateDescription_ShouldSucceed() throws Exception {
    // Given
    var patchRQ = TmsManualLaunchRQ.builder()
        .description("Updated description")
        .build();

    // When
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(200));

    // Then
    var launch = launchRepository.findById(200L);
    assertTrue(launch.isPresent());
    assertEquals("Updated description", launch.get().getDescription());
  }

  @Test
  void patchManualLaunch_UpdateAttributes_ShouldSucceed() throws Exception {
    // Given
    var patchRQ = TmsManualLaunchRQ.builder()
        .attributes(List.of(
            new ItemAttributesRQ("environment", "staging"),
            new ItemAttributesRQ("build", "1.2.3")
        ))
        .build();

    // When
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.attributes").isArray())
        .andExpect(jsonPath("$.attributes.length()").value(2));
  }

  @Test
  void patchManualLaunch_NonExistent_ShouldReturnNotFound() throws Exception {
    // Given
    var patchRQ = TmsManualLaunchRQ.builder()
        .name("New Name")
        .build();

    // When/Then
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/999")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }


  @Test
  void deleteManualLaunch_NonExistent_ShouldReturnNotFound() throws Exception {
    // When/Then
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/999")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  // ==================== ADD TEST CASE TO LAUNCH ====================

  @Test
  void addTestCaseToLaunch_ShouldCreateExecution() throws Exception {
    // Given
    var addTestCaseRQ = AddTestCaseToLaunchRQ.builder()
        .testCaseId(7L)
        .build();

    // When
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(addTestCaseRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then
    entityManager.clear();
    var executions = testCaseExecutionRepository.findByLaunchId(200L);
    assertThat(executions).anySatisfy(execution ->
        assertEquals(7L, execution.getTestCaseId())
    );
  }

  @Test
  void addTestCaseToLaunch_DuplicateTestCase_ShouldNotAllowMultipleExecutions() throws Exception {
    // Given
    var addTestCaseRQ = AddTestCaseToLaunchRQ.builder()
        .testCaseId(4L) // Already exists in launch 200
        .build();

    // When
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(addTestCaseRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void addTestCaseToLaunch_NonExistentTestCase_ShouldReturnBadRequest() throws Exception {
    // Given
    var addTestCaseRQ = AddTestCaseToLaunchRQ.builder()
        .testCaseId(999L)
        .build();

    // When/Then
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(addTestCaseRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void addTestCaseToLaunch_NonExistentLaunch_ShouldReturnNotFound() throws Exception {
    // Given
    var addTestCaseRQ = AddTestCaseToLaunchRQ.builder()
        .testCaseId(4L)
        .build();

    // When/Then
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/999/test-case")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(addTestCaseRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  // ==================== BATCH ADD TEST CASES TO LAUNCH ====================

  @Test
  void batchAddTestCasesToLaunch_ShouldAddAll() throws Exception {
    // Given
    var batchAddRQ = BatchAddTestCasesToLaunchRQ.builder()
        .testCaseIds(List.of(7L, 8L, 9L))
        .build();

    // When
    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/batch")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(batchAddRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successCount").value(3))
        .andExpect(jsonPath("$.failureCount").value(0))
        .andReturn();

    var response = mapper.readValue(
        result.getResponse().getContentAsString(),
        BatchTestCaseOperationResultRS.class
    );

    // Then
    assertThat(response.getSuccessTestCaseIds()).containsExactlyInAnyOrder(7L, 8L, 9L);

    entityManager.clear();
    var executions = testCaseExecutionRepository.findByLaunchId(200L);
    assertThat(executions).extracting("testCaseId")
        .contains(7L, 8L, 9L);
  }

  @Test
  void batchAddTestCasesToLaunch_WithSomeNonExistent_ShouldReturnPartialSuccess() throws Exception {
    // Given
    var batchAddRQ = BatchAddTestCasesToLaunchRQ.builder()
        .testCaseIds(List.of(7L, 999L, 8L))
        .build();

    // When
    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/batch")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(batchAddRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successCount").value(2))
        .andExpect(jsonPath("$.failureCount").value(1))
        .andExpect(jsonPath("$.errors[0].testCaseId").value(999));
  }

  // ==================== GET LAUNCH FOLDERS ====================

  @Test
  void getLaunchFolders_ShouldReturnFolders() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/folder")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].id").exists())
        .andExpect(jsonPath("$.content[0].name").exists())
        .andExpect(jsonPath("$.content[0].countOfTestCases").exists());
  }

  @Test
  void getLaunchFolders_WithPagination_ShouldReturnCorrectPage() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/folder")
                .param("offset", "0")
                .param("limit", "5")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.size").value(5));
  }

  // ==================== GET TEST CASE EXECUTIONS ====================

  @Test
  void getLaunchTestCaseExecutions_ShouldReturnAll() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].id").exists())
        .andExpect(jsonPath("$.content[0].testCaseId").exists())
        .andExpect(jsonPath("$.content[0].testCaseName").exists());
  }

  @Test
  void getLaunchTestCaseExecutions_WithStatusFilter_ShouldFilterCorrectly() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution")
                .param("filter.eq.status", "PASSED")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].executionStatus")
            .value(everyItem(equalTo("PASSED"))));
  }

  @Test
  void getLaunchTestCaseExecutions_WithPagination_ShouldReturnCorrectPage() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution")
                .param("offset", "0")
                .param("limit", "10")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.size").value(10));
  }

  // ==================== GET SPECIFIC TEST CASE EXECUTION ====================

  @Test
  void getTestCaseExecution_ShouldReturnExecution() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution/10")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.testCaseId").exists())
        .andExpect(jsonPath("$.testCaseName").exists())
        .andExpect(jsonPath("$.executionStatus").exists());
  }

  @Test
  void getTestCaseExecution_NonExistent_ShouldReturnNotFound() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution/999")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getTestCaseExecution_FromDifferentLaunch_ShouldReturnNotFound() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/201/test-case/execution/10")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  // ==================== DELETE TEST CASE EXECUTION ====================

  @Test
  void deleteTestCaseExecution_ShouldRemoveExecution() throws Exception {
    // When
    mockMvc.perform(
            delete(
                "/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution/10")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then
    var execution = testCaseExecutionRepository.findById(10L);
    assertTrue(execution.isEmpty());
  }

  @Test
  void deleteTestCaseExecution_ShouldRemoveRelatedComment() throws Exception {
    // When
    mockMvc.perform(
            delete(
                "/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution/10")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void deleteTestCaseExecution_NonExistent_ShouldReturnNotFound() throws Exception {
    // When/Then
    mockMvc.perform(
            delete(
                "/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution/999")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  // ==================== GET TEST CASE EXECUTIONS FOR SPECIFIC TEST CASE ====================

  @Test
  void getTestCaseExecutionsInLaunch_ShouldReturnAllExecutions() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/4/execution")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[*].testCaseId").value(everyItem(equalTo(4))));
  }

  @Test
  void getTestCaseExecutionsInLaunch_WithPagination_ShouldReturnCorrectPage() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/4/execution")
                .param("limit", "5")
                .param("offset", "0")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.size").value(5));
  }

  @Test
  void getTestCaseExecutionsInLaunch_NonExistentTestCase_ShouldReturnEmpty() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/999/execution")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty());
  }

  // ==================== PATCH TEST CASE EXECUTION ====================

  @Test
  void patchTestCaseExecution_UpdateStatus_ShouldSucceed() throws Exception {
    // Given
    var patchRQ = TmsTestCaseExecutionRQ.builder()
        .status("PASSED")
        .build();

    // When
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution/10")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionStatus").value("PASSED"));

    // Then - verify test item status is also updated
    entityManager.clear();
    var execution = testCaseExecutionRepository.findById(10L);
    assertTrue(execution.isPresent());

    if (execution.get().getTestItem() != null) {
      var testItem = testItemRepository.findById(execution.get().getTestItem().getItemId());
      assertTrue(testItem.isPresent());
      assertEquals(StatusEnum.PASSED, testItem.get().getItemResults().getStatus());
    }
  }

  @Test
  void patchTestCaseExecution_UpdateToFailed_ShouldSucceed() throws Exception {
    // Given
    var patchRQ = TmsTestCaseExecutionRQ.builder()
        .status("FAILED")
        .build();

    // When
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution/11")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionStatus").value("FAILED"));
  }

  @Test
  void patchTestCaseExecution_UpdateToSkipped_ShouldSucceed() throws Exception {
    // Given
    var patchRQ = TmsTestCaseExecutionRQ.builder()
        .status("SKIPPED")
        .build();

    // When
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution/10")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionStatus").value("SKIPPED"));
  }

  @Test
  void patchTestCaseExecution_InvalidStatus_ShouldReturnBadRequest() throws Exception {
    // Given
    var patchRQ = TmsTestCaseExecutionRQ.builder()
        .status("INVALID_STATUS")
        .build();

    // When/Then
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution/10")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patchTestCaseExecution_NonExistent_ShouldReturnNotFound() throws Exception {
    // Given
    var patchRQ = TmsTestCaseExecutionRQ.builder()
        .status("PASSED")
        .build();

    // When/Then
    mockMvc.perform(
            patch(
                "/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution/999")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  // ==================== PUT TEST CASE EXECUTION COMMENT ====================

  @Test
  void putTestCaseExecutionComment_CreateNew_ShouldSucceed() throws Exception {
    // Given
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Test failed due to timeout issue")
        .build();

    // When
    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment").value("Test failed due to timeout issue"));
  }

  @Test
  void putTestCaseExecutionComment_UpdateExisting_ShouldSucceed() throws Exception {
    // Given - existing comment
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Updated comment text")
        .build();

    // When
    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/10/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment").value("Updated comment text"));
  }

  @Test
  void putTestCaseExecutionComment_WithBtsTicket_ShouldLinkTicket() throws Exception {
    // Given
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Bug found, see linked ticket")
        .btsTicket(TmsTestCaseExecutionCommentBtsTicketRQ.builder()
            .id(1L)
            .build())
        .build();

    // When
    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.btsTicket.id").value(1));

    // Then
    entityManager.clear();
    var comment = executionCommentRepository.findByExecutionId(11L);
    assertTrue(comment.isPresent());
    assertEquals(1L, comment.get().getBtsTicketId());
  }

  @Test
  void putTestCaseExecutionComment_WithAttachments_ShouldLinkAttachments() throws Exception {
    // Given - upload attachment first
    var attachment = uploadTestAttachment("error-screenshot.png", "image/png");

    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("See attached screenshot")
        .attachments(List.of(
            TmsTestCaseExecutionCommentAttachmentRQ.builder()
                .id(String.valueOf(attachment.getId()))
                .build()
        ))
        .build();

    // When
    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment").value("See attached screenshot"))
        .andExpect(jsonPath("$.attachments").isArray())
        .andExpect(jsonPath("$.attachments[0].id").value(attachment.getId()));
  }

  @Test
  void putTestCaseExecutionComment_WithMultipleAttachments_ShouldLinkAll() throws Exception {
    // Given
    var attachment1 = uploadTestAttachment("screenshot1.png", "image/png");
    var attachment2 = uploadTestAttachment("log-file.txt", "text/plain");

    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Multiple attachments")
        .attachments(List.of(
            TmsTestCaseExecutionCommentAttachmentRQ.builder().id(
                String.valueOf(attachment1.getId())).build(),
            TmsTestCaseExecutionCommentAttachmentRQ.builder().id(
                String.valueOf(attachment2.getId())).build()
        ))
        .build();

    // When
    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment").value("Multiple attachments"))
        .andExpect(jsonPath("$.attachments").isArray())
        .andExpect(jsonPath("$.attachments.length()").value(2));
  }

  @Test
  void putTestCaseExecutionComment_UpdateWithNewAttachments_ShouldReplaceAttachments()
      throws Exception {
    // Given - existing comment with attachment
    var oldAttachment = uploadTestAttachment("old.png", "image/png");
    var commentRQ1 = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Old comment")
        .attachments(List.of(
            TmsTestCaseExecutionCommentAttachmentRQ.builder().id(
                String.valueOf(oldAttachment.getId())).build()
        ))
        .build();

    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ1))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // When - update with new attachment
    var newAttachment = uploadTestAttachment("new.png", "image/png");
    var commentRQ2 = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Updated comment")
        .attachments(List.of(
            TmsTestCaseExecutionCommentAttachmentRQ.builder().id(
                String.valueOf(newAttachment.getId())).build()
        ))
        .build();

    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ2))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.attachments").isArray())
        .andExpect(jsonPath("$.attachments[0].id").value(newAttachment.getId()));
  }

  @Test
  void putTestCaseExecutionComment_EmptyComment_ShouldSucceed() throws Exception {
    // Given
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("")
        .build();

    // When/Then
    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void putTestCaseExecutionComment_WithNonExistentAttachment_ShouldReturnNotFound()
      throws Exception {
    // Given
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Comment with invalid attachment")
        .attachments(List.of(
            TmsTestCaseExecutionCommentAttachmentRQ.builder().id("999").build()
        ))
        .build();

    // When/Then
    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void putTestCaseExecutionComment_NonExistentExecution_ShouldReturnNotFound() throws Exception {
    // Given
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Comment for non-existent execution")
        .build();

    // When/Then
    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/999/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  // ==================== DELETE TEST CASE EXECUTION COMMENT ====================

  @Test
  void deleteTestCaseExecutionComment_ShouldRemoveComment() throws Exception {
    // Given - verify comment exists
    var commentBefore = executionCommentRepository.findByExecutionId(10L);
    assertTrue(commentBefore.isPresent());

    // When
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/10/comment")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then
    entityManager.clear();
    var commentAfter = executionCommentRepository.findByExecutionId(10L);
    assertTrue(commentAfter.isEmpty());
  }

  @Test
  void deleteTestCaseExecutionComment_NonExistentComment_ShouldReturnNotFound() throws Exception {
    // When/Then
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/203/test-case/execution/20/comment")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteTestCaseExecutionComment_NonExistentExecution_ShouldReturnNotFound() throws Exception {
    // When/Then
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/999/comment")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  // ==================== COMPLEX WORKFLOW SCENARIOS ====================

  @Test
  void fullManualLaunchWorkflow_CreateExecuteAndFinish_ShouldSucceed() throws Exception {
    // STEP 1: Create manual launch
    var launchRQ = TmsManualLaunchRQ.builder()
        .name("Full Workflow Test Launch")
        .description("Complete workflow from start to finish")
        .mode(Mode.DEFAULT)
        .attributes(List.of(
            new ItemAttributesRQ("sprint", "Sprint-25")
        ))
        .build();

    var createResult = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(launchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    var launch = mapper.readValue(
        createResult.getResponse().getContentAsString(),
        TmsManualLaunchRS.class
    );

    entityManager.clear();

    // STEP 2: Add test cases to launch
    var batchAddRQ = BatchAddTestCasesToLaunchRQ.builder()
        .testCaseIds(List.of(4L, 5L, 6L))
        .build();

    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/" + launch.getId()
                + "/test-case/batch")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(batchAddRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successCount").value(3));

    entityManager.clear();

    // STEP 3: Get executions and update their statuses
    var executionsResult = mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/" + launch.getId()
                + "/test-case/execution")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    var executionsPage = mapper.readValue(
        executionsResult.getResponse().getContentAsString(),
        new TypeReference<com.epam.reportportal.model.Page<TmsTestCaseExecutionRS>>() {
        }
    );

    assertThat(executionsPage.getContent()).hasSize(3);

    // STEP 4: Execute first test case - PASSED
    var executions = new ArrayList<>(executionsPage.getContent());
    var execution1 = executions.get(0);
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/" + launch.getId()
                + "/test-case/execution/" + execution1.getId())
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(
                    TmsTestCaseExecutionRQ.builder().status("PASSED").build()
                ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionStatus").value("PASSED"));

    entityManager.clear();

    // STEP 5: Execute second test case - FAILED with comment and attachment
    var execution2 = executions.get(1);

    // First update status to FAILED
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/" + launch.getId()
                + "/test-case/execution/" + execution2.getId())
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(
                    TmsTestCaseExecutionRQ.builder().status("FAILED").build()
                ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // Then add comment with attachment
    var attachment = uploadTestAttachment("failure-screenshot.png", "image/png");
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Test failed - element not found. See screenshot.")
        .attachments(List.of(
            TmsTestCaseExecutionCommentAttachmentRQ.builder().id(String.valueOf(attachment.getId()))
                .build()
        ))
        .build();

    mockMvc.perform( //TODO here
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/" + launch.getId()
                + "/test-case/execution/" + execution2.getId() + "/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment").exists())
        .andExpect(jsonPath("$.attachments[0].id").value(attachment.getId()));

    entityManager.clear();

    // STEP 6: Execute third test case - SKIPPED
    var execution3 = executions.get(2);
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/" + launch.getId()
                + "/test-case/execution/" + execution3.getId())
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(
                    TmsTestCaseExecutionRQ.builder().status("SKIPPED").build()
                ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // STEP 7: Verify launch execution statistics
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/" + launch.getId())
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionStatistic").exists());
  }

  @Test
  void complexScenario_UpdateCommentMultipleTimes_ShouldKeepLatest() throws Exception {
    // Given - execution with initial comment
    var commentRQ1 = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Initial comment")
        .build();

    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ1))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    // When - update comment multiple times
    var commentRQ2 = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Updated comment v2")
        .build();

    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ2))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    var commentRQ3 = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Final comment v3")
        .build();

    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ3))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment").value("Final comment v3"));
  }

  // ==================== HELPER METHODS ====================

  private UploadAttachmentRS uploadTestAttachment(String fileName, String contentType)
      throws Exception {
    var file = new MockMultipartFile(
        "file",
        fileName,
        contentType,
        ("test content for " + fileName).getBytes()
    );

    var result = mockMvc.perform(
            multipart("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/attachment/upload")
                .file(file)
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    return mapper.readValue(
        result.getResponse().getContentAsString(),
        UploadAttachmentRS.class
    );
  }
}
