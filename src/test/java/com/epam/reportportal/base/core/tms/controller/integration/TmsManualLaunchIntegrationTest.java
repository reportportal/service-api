package com.epam.reportportal.base.core.tms.controller.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.epam.reportportal.base.core.tms.dto.AddTestCaseToLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentAttachmentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.reportportal.base.core.tms.dto.UploadAttachmentRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchAddTestCasesToLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteManualLaunchesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteTestCaseExecutionsRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteTestCaseExecutionsResultRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.infrastructure.persistence.dao.ItemAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseExecutionCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseExecutionRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.reporting.ItemAttributesRQ;
import com.epam.reportportal.base.reporting.Mode;
import com.epam.reportportal.base.ws.BaseMvcTest;
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
import org.springframework.transaction.annotation.Transactional;

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
  @Autowired
  private ItemAttributeRepository itemAttributeRepository;
  @PersistenceContext
  private EntityManager entityManager;

  @Test
  void createManualLaunch_WithAllFields_ShouldSucceed() throws Exception {
    var launchRQ = TmsManualLaunchRQ.builder()
        .name("Manual Launch Full")
        .uuid("550e8400-e29b-41d4-a716-446655440999")
        .startTime("2024-01-20T10:00:00Z")
        .mode(Mode.DEFAULT)
        .testPlan(new com.epam.reportportal.base.core.tms.dto.TmsManualLaunchTestPlanRQ(6L))
        .description("Full manual launch with all fields")
        .testCaseIds(List.of(4L, 5L))
        .attributes(List.of(
            new ItemAttributesRQ("priority", "high"),
            new ItemAttributesRQ("team", "qa")
        ))
        .build();

    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(launchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Manual Launch Full"))
        .andExpect(jsonPath("$.description").value("Full manual launch with all fields"))
        .andExpect(jsonPath("$.owner").exists())
        .andExpect(jsonPath("$.owner.id").exists())
        .andExpect(jsonPath("$.type").exists())
        .andExpect(jsonPath("$.testPlan").exists())
        .andExpect(jsonPath("$.testPlan.id").value(6L))
        .andExpect(jsonPath("$.mode").value("DEFAULT"))
        .andExpect(jsonPath("$.status").doesNotExist())
        .andReturn();

    var response = mapper.readValue(result.getResponse().getContentAsString(), TmsManualLaunchRS.class);
    var launch = launchRepository.findById(response.getId());
    assertTrue(launch.isPresent());
    assertEquals(LaunchTypeEnum.MANUAL, launch.get().getLaunchType());
    assertEquals("Manual Launch Full", launch.get().getName());
    assertEquals("Full manual launch with all fields", launch.get().getDescription());
    assertThat(launch.get().getAttributes()).hasSize(2);
  }

  @Test
  void createManualLaunch_WithTestPlanBatchMode_ShouldAddAllTestCasesFromPlan() throws Exception {
    var launchRQ = TmsManualLaunchRQ.builder()
        .name("Launch with Test Plan Batch")
        .testPlan(new com.epam.reportportal.base.core.tms.dto.TmsManualLaunchTestPlanRQ(1L))
        .build();

    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(launchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Launch with Test Plan Batch"))
        .andExpect(jsonPath("$.testPlan.id").value(1L))
        .andExpect(jsonPath("$.executionStatistic").exists())
        .andExpect(jsonPath("$.executionStatistic.total").exists())
        .andExpect(jsonPath("$.executionStatistic.toRun").exists())
        .andReturn();

    var response = mapper.readValue(result.getResponse().getContentAsString(), TmsManualLaunchRS.class);
    entityManager.clear();
    var executions = testCaseExecutionRepository.findByLaunchId(response.getId());
    assertThat(executions).hasSizeGreaterThan(3);
    var testCaseIds = executions.stream().map(exec -> exec.getTestCaseId()).toList();
    assertThat(testCaseIds).contains(4L, 5L, 6L, 13L, 14L, 15L, 16L);
  }

  @Test
  void createManualLaunch_WithSpecificTestCasesBatchMode_ShouldAddOnlySpecifiedTestCases() throws Exception {
    var launchRQ = TmsManualLaunchRQ.builder()
        .name("Launch with Specific Test Cases Batch")
        .testPlan(new com.epam.reportportal.base.core.tms.dto.TmsManualLaunchTestPlanRQ(1L))
        .testCaseIds(List.of(4L, 5L))
        .build();

    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(launchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Launch with Specific Test Cases Batch"))
        .andExpect(jsonPath("$.testPlan.id").value(1L))
        .andExpect(jsonPath("$.executionStatistic").exists())
        .andExpect(jsonPath("$.executionStatistic.total").value(2))
        .andExpect(jsonPath("$.executionStatistic.toRun").value(2))
        .andReturn();

    var response = mapper.readValue(result.getResponse().getContentAsString(), TmsManualLaunchRS.class);
    entityManager.clear();
    var executions = testCaseExecutionRepository.findByLaunchId(response.getId());
    assertThat(executions).hasSize(2);
    assertThat(executions).extracting("testCaseId").containsExactlyInAnyOrder(4L, 5L);
  }

  @Test
  void createManualLaunch_WithTestCaseAttributes_ShouldMapToItemAttributes() throws Exception {
    var launchRQ = TmsManualLaunchRQ.builder()
        .name("Launch for Attribute Mapping Test")
        .testPlan(new com.epam.reportportal.base.core.tms.dto.TmsManualLaunchTestPlanRQ(6L))
        .testCaseIds(List.of(4L))
        .build();

    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(launchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    var response = mapper.readValue(result.getResponse().getContentAsString(), TmsManualLaunchRS.class);
    entityManager.clear();
    var executions = testCaseExecutionRepository.findByLaunchId(response.getId());
    assertThat(executions).hasSize(1);
    var testItem = executions.getFirst().getTestItem();
    assertThat(testItem).isNotNull();
    var itemAttributes = itemAttributeRepository.findAllByTestItem(testItem);
    assertThat(itemAttributes).isNotEmpty();
    var tagAttributes = itemAttributes.stream().filter(attr -> "tag".equals(attr.getKey())).toList();
    assertThat(tagAttributes).isNotEmpty();
    assertThat(tagAttributes).anySatisfy(attr -> {
      assertEquals("tag", attr.getKey());
      assertEquals("test4", attr.getValue());
      assertEquals(false, attr.isSystem());
    });
  }

  @Test
  void createManualLaunch_WithMultipleTestCaseAttributes_ShouldMapAllAttributes() throws Exception {
    var launchRQ = TmsManualLaunchRQ.builder()
        .name("Launch for Multiple Attributes Test")
        .testPlan(new com.epam.reportportal.base.core.tms.dto.TmsManualLaunchTestPlanRQ(6L))
        .testCaseIds(List.of(37L))
        .build();

    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(launchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    var response = mapper.readValue(result.getResponse().getContentAsString(), TmsManualLaunchRS.class);
    entityManager.clear();
    var executions = testCaseExecutionRepository.findByLaunchId(response.getId());
    var testItem = executions.get(0).getTestItem();
    var itemAttributes = itemAttributeRepository.findAllByTestItem(testItem);
    var tagAttributes = itemAttributes.stream().filter(attr -> "tag".equals(attr.getKey())).toList();

    assertThat(tagAttributes).hasSize(2);
    assertThat(tagAttributes).extracting("value").containsExactlyInAnyOrder("test1", "test2");
    assertThat(tagAttributes).allSatisfy(attr -> {
      assertEquals("tag", attr.getKey());
      assertEquals(false, attr.isSystem());
    });
  }

  @Test
  void createManualLaunch_MinimalData_ShouldSucceed() throws Exception {
    var launchRQ = TmsManualLaunchRQ.builder()
        .name("Minimal Manual Launch")
        .testPlan(new com.epam.reportportal.base.core.tms.dto.TmsManualLaunchTestPlanRQ(6L))
        .build();

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
    var launchRQ = TmsManualLaunchRQ.builder()
        .name("Launch with Test Cases")
        .testPlan(new com.epam.reportportal.base.core.tms.dto.TmsManualLaunchTestPlanRQ(6L))
        .testCaseIds(List.of(4L, 5L, 6L))
        .build();

    var result = mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(launchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    var response = mapper.readValue(result.getResponse().getContentAsString(), TmsManualLaunchRS.class);
    var executions = testCaseExecutionRepository.findByLaunchId(response.getId());
    assertThat(executions).hasSize(3);
    assertThat(executions).extracting("testCaseId").containsExactlyInAnyOrder(4L, 5L, 6L);
  }

  @Test
  void getManualLaunches_ShouldReturnOnlyManualLaunches() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isNotEmpty())
        .andExpect(jsonPath("$.content[0].description").exists())
        .andExpect(jsonPath("$.content[0].owner").exists())
        .andExpect(jsonPath("$.content[0].owner.email").exists())
        .andExpect(jsonPath("$.content[0].status").doesNotExist());
  }

  @Test
  void getManualLaunches_WithPagination_ShouldReturnCorrectPage() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .param("offset", "0")
                .param("limit", "2")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.size").value(2))
        .andExpect(jsonPath("$.page.number").value(1))
        .andExpect(jsonPath("$.content[*].description").exists())
        .andExpect(jsonPath("$.content[*].owner").exists());
  }

  @Test
  void getManualLaunches_WithSorting_ShouldReturnSorted() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .param("sort", "startTime,desc")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void getManualLaunches_WithStatusFilter_ShouldFilterCorrectly() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .param("filter.eq.status", "IN_PROGRESS")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].status").doesNotExist());
  }

  @Test
  void getManualLaunches_WithNameFilter_ShouldFilterCorrectly() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .param("filter.cnt.name", "Manual Launch 1")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void getManualLaunchById_ShouldReturnLaunchWithAllFields() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(200))
        .andExpect(jsonPath("$.name").exists())
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.owner").exists())
        .andExpect(jsonPath("$.owner.id").exists())
        .andExpect(jsonPath("$.owner.email").exists())
        .andExpect(jsonPath("$.type").exists())
        .andExpect(jsonPath("$.testPlan").exists())
        .andExpect(jsonPath("$.testPlan.id").exists())
        .andExpect(jsonPath("$.testPlan.name").exists())
        .andExpect(jsonPath("$.startTime").exists())
        .andExpect(jsonPath("$.status").doesNotExist())
        .andExpect(jsonPath("$.executionStatistic").exists());
  }

  @Test
  void getManualLaunchById_ShouldValidateDescriptionContent() throws Exception {
    var result = mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    var response = mapper.readValue(result.getResponse().getContentAsString(), TmsManualLaunchRS.class);
    assertThat(response.getDescription()).isNotNull();
    assertThat(response.getOwner()).isNotNull();
    assertThat(response.getOwner().getEmail()).isNotNull();
    assertThat(response.getType()).isNotNull();
    assertThat(response.getTestPlan()).isNotNull();
    assertThat(response.getTestPlan().getId()).isNotNull();
  }

  @Test
  void getManualLaunchById_ShouldReturnCorrectOwnerInformation() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.owner.id").exists())
        .andExpect(jsonPath("$.owner.email").isString())
        .andExpect(jsonPath("$.owner.email").isNotEmpty());
  }

  @Test
  void getManualLaunchById_ShouldReturnCorrectTestPlanInformation() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.testPlan.id").isNumber())
        .andExpect(jsonPath("$.testPlan.name").isString());
  }

  @Test
  void getManualLaunchById_NonExistent_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/999")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(containsString("999")));
  }

  @Test
  void getManualLaunchById_FromDifferentProject_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + DEFAULT_PROJECT_KEY + "/launch/manual/200")
                .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void patchManualLaunch_UpdateName_ShouldSucceed() throws Exception {
    var patchRQ = TmsManualLaunchRQ.builder().name("Updated Launch Name").build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Launch Name"));

    var launch = launchRepository.findById(200L);
    assertTrue(launch.isPresent());
    assertEquals("Updated Launch Name", launch.get().getName());
  }

  @Test
  void patchManualLaunch_UpdateDescription_ShouldSucceed() throws Exception {
    var patchRQ = TmsManualLaunchRQ.builder().description("Updated description").build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(200));

    var launch = launchRepository.findById(200L);
    assertTrue(launch.isPresent());
    assertEquals("Updated description", launch.get().getDescription());
  }

  @Test
  void patchManualLaunch_UpdateAttributes_ShouldSucceed() throws Exception {
    var patchRQ = TmsManualLaunchRQ.builder()
        .attributes(List.of(
            new ItemAttributesRQ("environment", "staging"),
            new ItemAttributesRQ("build", "1.2.3")
        ))
        .build();

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
  void patchManualLaunch_UnlinkTestPlan_ShouldSucceed() throws Exception {
    var patchRQ = TmsManualLaunchRQ.builder()
        .testPlan(new com.epam.reportportal.base.core.tms.dto.TmsManualLaunchTestPlanRQ())
        .build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.testPlan").exists())
        .andExpect(jsonPath("$.testPlan.id").doesNotExist());
  }

  @Test
  void patchManualLaunch_NonExistent_ShouldReturnNotFound() throws Exception {
    var patchRQ = TmsManualLaunchRQ.builder().name("New Name").build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/999")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteManualLaunch_InProgress_ShouldSucceed() throws Exception {
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/202")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    var launch = launchRepository.findById(202L);
    assertTrue(launch.isEmpty());
  }

  @Test
  void batchDeleteManualLaunches_InProgress_ShouldSucceed() throws Exception {
    var batchDeleteRQ = BatchDeleteManualLaunchesRQ.builder().launchIds(List.of(202L)).build();

    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(batchDeleteRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    var launch = launchRepository.findById(202L);
    assertTrue(launch.isEmpty());
  }

  @Test
  void deleteManualLaunch_NonExistent_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/999")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void addTestCaseToLaunch_ShouldCreateExecution() throws Exception {
    var addTestCaseRQ = AddTestCaseToLaunchRQ.builder().testCaseId(7L).build();

    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(addTestCaseRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    entityManager.clear();
    var executions = testCaseExecutionRepository.findByLaunchId(200L);
    assertThat(executions).anySatisfy(execution -> assertEquals(7L, execution.getTestCaseId()));
  }

  @Test
  void addTestCaseToLaunch_DuplicateTestCase_ShouldNotAllowMultipleExecutions() throws Exception {
    var addTestCaseRQ = AddTestCaseToLaunchRQ.builder().testCaseId(4L).build();

    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(addTestCaseRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void addTestCaseToLaunch_NonExistentTestCase_ShouldReturnBadRequest() throws Exception {
    var addTestCaseRQ = AddTestCaseToLaunchRQ.builder().testCaseId(999L).build();

    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(addTestCaseRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void addTestCaseToLaunch_NonExistentLaunch_ShouldReturnNotFound() throws Exception {
    var addTestCaseRQ = AddTestCaseToLaunchRQ.builder().testCaseId(4L).build();

    mockMvc.perform(
            post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/999/test-case")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(addTestCaseRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void batchAddTestCasesToLaunch_ShouldAddAll() throws Exception {
    var batchAddRQ = BatchAddTestCasesToLaunchRQ.builder().testCaseIds(List.of(7L, 8L, 9L)).build();

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
    assertThat(response.getSuccessTestCaseIds()).containsExactlyInAnyOrder(7L, 8L, 9L);

    entityManager.clear();
    var executions = testCaseExecutionRepository.findByLaunchId(200L);
    assertThat(executions).extracting("testCaseId").contains(7L, 8L, 9L);
  }

  @Test
  void batchAddTestCasesToLaunch_WithSomeNonExistent_ShouldReturnPartialSuccess() throws Exception {
    var batchAddRQ = BatchAddTestCasesToLaunchRQ.builder().testCaseIds(List.of(7L, 999L, 8L)).build();

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

  @Test
  void getLaunchFolders_ShouldReturnFolders() throws Exception {
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
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/folder")
                .param("offset", "0")
                .param("limit", "5")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.size").value(5));
  }

  @Test
  @Transactional
  void getLaunchFolders_WithFilters_ShouldReturnFilteredFolders() throws Exception {
    var executions = testCaseExecutionRepository.findByLaunchId(201L);
    assertThat(executions).isNotEmpty();

    var execToUpdate = executions.getFirst();
    execToUpdate.setName("Search Functionality Test");
    execToUpdate.setPriority("P1");
    testCaseExecutionRepository.save(execToUpdate);

    var testItem = execToUpdate.getTestItem();
    var attribute = new ItemAttribute("tag", "suite_type", false);
    attribute.setTestItem(testItem);
    itemAttributeRepository.save(attribute);

    entityManager.flush();
    entityManager.clear();

    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/201/folder")
                .param("filter.cnt.testCaseName", "Search")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].countOfTestCases", equalTo(1)));

    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/201/folder")
                .param("filter.eq.testCasePriority", "P1")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].countOfTestCases", equalTo(1)));

    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/201/folder")
                .param("filter.has.testCaseAttributeKey", "suite_type")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].countOfTestCases", equalTo(1)));
  }

  @Test
  void getLaunchTestCaseExecutions_ShouldReturnAll() throws Exception {
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
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution")
                .param("filter.eq.status", "PASSED")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].executionStatus").value(everyItem(equalTo("PASSED"))));
  }

  @Test
  void getLaunchTestCaseExecutions_WithPagination_ShouldReturnCorrectPage() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution")
                .param("offset", "0")
                .param("limit", "10")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page.size").value(10));
  }

  @Test
  void getTestCaseExecution_ShouldReturnExecution() throws Exception {
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
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/execution/999")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getTestCaseExecution_FromDifferentLaunch_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/201/test-case/execution/10")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteTestCaseExecution_ShouldRemoveExecution() throws Exception {
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/10")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    var execution = testCaseExecutionRepository.findById(10L);
    assertTrue(execution.isEmpty());
  }

  @Test
  void deleteTestCaseExecution_ShouldRemoveRelatedComment() throws Exception {
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/10")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void deleteTestCaseExecution_NonExistent_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/999")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getTestCaseExecutionsInLaunch_ShouldReturnAllExecutions() throws Exception {
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/4/execution")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[*].testCaseId").value(everyItem(equalTo(4))));
  }

  @Test
  void getTestCaseExecutionsInLaunch_WithPagination_ShouldReturnCorrectPage() throws Exception {
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
    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200/test-case/999/execution")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  void patchTestCaseExecution_UpdateStatus_ShouldSucceed() throws Exception {
    var patchRQ = TmsTestCaseExecutionRQ.builder().status("PASSED").build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/10")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionStatus").value("PASSED"));

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
    var patchRQ = TmsTestCaseExecutionRQ.builder().status("FAILED").build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionStatus").value("FAILED"));
  }

  @Test
  void patchTestCaseExecution_UpdateToSkipped_ShouldSucceed() throws Exception {
    var patchRQ = TmsTestCaseExecutionRQ.builder().status("SKIPPED").build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/10")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionStatus").value("SKIPPED"));
  }

  @Test
  void patchTestCaseExecution_InvalidStatus_ShouldReturnBadRequest() throws Exception {
    var patchRQ = TmsTestCaseExecutionRQ.builder().status("INVALID_STATUS").build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/10")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patchTestCaseExecution_NonExistent_ShouldReturnNotFound() throws Exception {
    var patchRQ = TmsTestCaseExecutionRQ.builder().status("PASSED").build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/999")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void putTestCaseExecutionComment_CreateNew_ShouldSucceed() throws Exception {
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Test failed due to timeout issue")
        .build();

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
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder().comment("Updated comment text").build();

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
  void putTestCaseExecutionComment_WithAttachments_ShouldLinkAttachments() throws Exception {
    var attachment = uploadTestAttachment("error-screenshot.png", "image/png");
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("See attached screenshot")
        .attachments(List.of(
            TmsTestCaseExecutionCommentAttachmentRQ.builder()
                .id(String.valueOf(attachment.getId()))
                .build()
        ))
        .build();

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
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder().comment("").build();

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
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Comment with invalid attachment")
        .attachments(List.of(
            TmsTestCaseExecutionCommentAttachmentRQ.builder().id("999").build()
        ))
        .build();

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
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Comment for non-existent execution")
        .build();

    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/999/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteTestCaseExecutionComment_ShouldRemoveComment() throws Exception {
    var commentBefore = executionCommentRepository.findByExecutionId(10L);
    assertTrue(commentBefore.isPresent());

    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/10/comment")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    entityManager.clear();
    var commentAfter = executionCommentRepository.findByExecutionId(10L);
    assertTrue(commentAfter.isEmpty());
  }

  @Test
  void deleteTestCaseExecutionComment_NonExistentComment_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/203/test-case/execution/20/comment")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteTestCaseExecutionComment_NonExistentExecution_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/999/comment")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void fullManualLaunchWorkflow_CreateExecuteAndFinish_ShouldSucceed() throws Exception {
    var launchRQ = TmsManualLaunchRQ.builder()
        .name("Full Workflow Test Launch")
        .description("Complete workflow from start to finish")
        .testPlan(new com.epam.reportportal.base.core.tms.dto.TmsManualLaunchTestPlanRQ(6L))
        .mode(Mode.DEFAULT)
        .attributes(List.of(new ItemAttributesRQ("sprint", "Sprint-25")))
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

    var executionsResult = mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/" + launch.getId()
                + "/test-case/execution")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn();

    var executionsPage = mapper.readValue(
        executionsResult.getResponse().getContentAsString(),
        new TypeReference<com.epam.reportportal.base.model.Page<TmsTestCaseExecutionRS>>() {
        }
    );

    assertThat(executionsPage.getContent()).hasSize(3);

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

    var execution2 = executions.get(1);
    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/" + launch.getId()
                + "/test-case/execution/" + execution2.getId())
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(
                    TmsTestCaseExecutionRQ.builder().status("FAILED").build()
                ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    var attachment = uploadTestAttachment("failure-screenshot.png", "image/png");
    var commentRQ = TmsTestCaseExecutionCommentRQ.builder()
        .comment("Test failed - element not found. See screenshot.")
        .attachments(List.of(
            TmsTestCaseExecutionCommentAttachmentRQ.builder().id(String.valueOf(attachment.getId()))
                .build()
        ))
        .build();

    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/" + launch.getId()
                + "/test-case/execution/" + execution2.getId() + "/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment").exists())
        .andExpect(jsonPath("$.attachments[0].id").value(attachment.getId()));

    entityManager.clear();

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

    mockMvc.perform(
            get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/" + launch.getId())
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionStatistic").exists());
  }

  @Test
  void complexScenario_UpdateCommentMultipleTimes_ShouldKeepLatest() throws Exception {
    var commentRQ1 = TmsTestCaseExecutionCommentRQ.builder().comment("Initial comment").build();

    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ1))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    var commentRQ2 = TmsTestCaseExecutionCommentRQ.builder().comment("Updated comment v2").build();

    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ2))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    var commentRQ3 = TmsTestCaseExecutionCommentRQ.builder().comment("Final comment v3").build();

    mockMvc.perform(
            put("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11/comment")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRQ3))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment").value("Final comment v3"));
  }

  @Test
  void batchDeleteTestCaseExecutions_AllValid_ShouldDeleteAll() throws Exception {
    var batchDeleteRQ = BatchDeleteTestCaseExecutionsRQ.builder()
        .executionIds(List.of(10L, 11L))
        .build();

    var result = mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(batchDeleteRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successCount").value(2))
        .andExpect(jsonPath("$.failureCount").value(0))
        .andReturn();

    var response = mapper.readValue(
        result.getResponse().getContentAsString(),
        BatchDeleteTestCaseExecutionsResultRS.class
    );
    assertThat(response.getSuccessExecutionIds()).containsExactlyInAnyOrder(10L, 11L);
  }

  @Test
  void batchDeleteTestCaseExecutions_SomeNonExistent_ShouldReturnPartialSuccess() throws Exception {
    var batchDeleteRQ = BatchDeleteTestCaseExecutionsRQ.builder()
        .executionIds(List.of(10L, 999L))
        .build();

    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(batchDeleteRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successCount").value(1))
        .andExpect(jsonPath("$.failureCount").value(1))
        .andExpect(jsonPath("$.errors[0].executionId").value(999));
  }

  @Test
  void batchDeleteTestCaseExecutions_AllNonExistent_ShouldReturnAllErrors() throws Exception {
    var batchDeleteRQ = BatchDeleteTestCaseExecutionsRQ.builder()
        .executionIds(List.of(998L, 999L))
        .build();

    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(batchDeleteRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successCount").value(0))
        .andExpect(jsonPath("$.failureCount").value(2));
  }

  @Test
  void batchDeleteTestCaseExecutions_EmptyList_ShouldReturnBadRequest() throws Exception {
    var batchDeleteRQ = BatchDeleteTestCaseExecutionsRQ.builder().executionIds(List.of()).build();

    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(batchDeleteRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void batchDeleteTestCaseExecutions_NonExistentLaunch_ShouldReturnNotFound() throws Exception {
    var batchDeleteRQ = BatchDeleteTestCaseExecutionsRQ.builder().executionIds(List.of(10L)).build();

    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/999/test-case/execution")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(batchDeleteRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void batchDeleteTestCaseExecutions_ExecutionFromDifferentLaunch_ShouldReturnError()
      throws Exception {
    var batchDeleteRQ = BatchDeleteTestCaseExecutionsRQ.builder().executionIds(List.of(10L)).build();

    mockMvc.perform(
            delete("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/201/test-case/execution")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(batchDeleteRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successCount").value(0))
        .andExpect(jsonPath("$.failureCount").value(1))
        .andExpect(jsonPath("$.errors[0].executionId").value(10))
        .andExpect(jsonPath("$.errors[0].errorMessage").value(
            containsString("not found in launch 201")));
  }

  @Test
  void patchManualLaunch_ClearAttributes_ShouldSucceed() throws Exception {
    var patchRQ = TmsManualLaunchRQ.builder()
        .attributes(java.util.Collections.emptyList())
        .build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/launch/manual/200")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.attributes").isEmpty());

    var launch = launchRepository.findById(200L);
    assertTrue(launch.isPresent());
    assertTrue(launch.get().getAttributes().isEmpty());
  }

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

    return mapper.readValue(result.getResponse().getContentAsString(), UploadAttachmentRS.class);
  }

  @Test
  void patchTestCaseExecution_WithNewComment_ShouldCreateComment() throws Exception {
    var patchRQ = TmsTestCaseExecutionRQ.builder()
        .status("PASSED")
        .executionComment(TmsTestCaseExecutionCommentRQ.builder()
            .comment("New comment added via patch")
            .build())
        .build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/11")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionStatus").value("PASSED"))
        .andExpect(jsonPath("$.executionComment.comment").value("New comment added via patch"));

    entityManager.clear();
    var comment = executionCommentRepository.findByExecutionId(11L);
    assertTrue(comment.isPresent());
  }

  @Test
  void patchTestCaseExecution_WithPartialComment_ShouldUpdateComment() throws Exception {
    var patchRQ = TmsTestCaseExecutionRQ.builder()
        .executionComment(TmsTestCaseExecutionCommentRQ.builder()
            .comment("Updated partial comment via patch")
            .build())
        .build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/10")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionComment.comment").value(
            "Updated partial comment via patch"));
  }

  @Test
  void patchTestCaseExecution_WithEmptyComment_ShouldDeleteComment() throws Exception {
    var patchRQ = TmsTestCaseExecutionRQ.builder()
        .executionComment(TmsTestCaseExecutionCommentRQ.builder().build())
        .build();

    mockMvc.perform(
            patch("/v1/project/" + SUPERADMIN_PROJECT_KEY
                + "/launch/manual/200/test-case/execution/10")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(patchRQ))
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.executionComment").doesNotExist());
  }
}