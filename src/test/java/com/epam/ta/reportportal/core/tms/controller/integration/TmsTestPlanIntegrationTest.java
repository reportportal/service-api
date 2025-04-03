package com.epam.ta.reportportal.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.core.tms.db.entity.TmsMilestone;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestPlanRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:konstantin_shaplyko@epam.com">Konstantin Shaplyko</a>
 */
@Sql("/db/tms/tms-product-version/tms-test-plan-fill.sql")
@ExtendWith(MockitoExtension.class)
public class TmsTestPlanIntegrationTest extends BaseMvcTest {

  @Autowired
  private TmsTestPlanRepository testPlanRepository;

  @Test
  void createTestPlanIntegrationTest() throws Exception {
    TmsTestPlanAttributeRQ attribute = new TmsTestPlanAttributeRQ();
    attribute.setValue("value3");
    attribute.setAttributeId(3L);

    TmsTestPlanRQ tmsTestPlan = new TmsTestPlanRQ();
    tmsTestPlan.setName("name3");
    tmsTestPlan.setDescription("description3");
    tmsTestPlan.setEnvironmentId(3L);
    tmsTestPlan.setProductVersionId(3L);
    tmsTestPlan.setMilestoneIds(List.of(3L));
    tmsTestPlan.setAttributes(List.of(attribute));

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(tmsTestPlan);

    mockMvc.perform(post("/project/3/tms/test-plan")
                    .contentType("application/json")
                    .content(jsonContent)
                    .with(token(oAuthHelper.getSuperadminToken())))
            .andExpect(status().isOk());

    Optional<TmsTestPlan> testPlan = testPlanRepository.findById(1L);

    assertTrue(testPlan.isPresent());
    assertEquals(tmsTestPlan.getName(), testPlan.get().getName());
    assertEquals(tmsTestPlan.getDescription(), testPlan.get().getDescription());
    assertEquals(tmsTestPlan.getEnvironmentId(), testPlan.get().getEnvironment().getId());
    TmsMilestone[] versionArray = testPlan.get().getMilestones()
            .toArray(new TmsMilestone[0]);
    assertEquals(tmsTestPlan.getMilestoneIds().get(0), versionArray[0].getId());
    assertEquals(tmsTestPlan.getProductVersionId(), testPlan.get().getProductVersion().getId());
  }

  @Test
  void getTestPlansByCriteriaIntegrationTest() throws Exception {
    Optional<TmsTestPlan> testPlan = testPlanRepository.findById(4L);

    mockMvc.perform(get("/project/4/tms/test-plan")
            .param("environmentId", "4")
            .param("productVersionId", "4")
            .param("page", "0")
            .param("size", "1")
            .with(token(oAuthHelper.getSuperadminToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(testPlan.get().getId()))
            .andExpect(jsonPath("$.content[0].name").value(testPlan.get().getName()))
            .andExpect(jsonPath("$.content[0].description").value(testPlan.get().getDescription()))
            .andExpect(jsonPath("$.content[0].environment.id").value(testPlan.get()
                                                               .getEnvironment().getId()))
            .andExpect(jsonPath("$.content[0].productVersion.id").value(testPlan.get()
                                                                .getProductVersion().getId()));
  }

  @Test
  void updateTestPlanIntegrationTest() throws Exception {
    TmsTestPlanAttributeRQ attribute = new TmsTestPlanAttributeRQ();
    attribute.setValue("value5");
    attribute.setAttributeId(5L);

    TmsTestPlanRQ tmsTestPlan = new TmsTestPlanRQ();
    tmsTestPlan.setName("updated_name5");
    tmsTestPlan.setDescription("updated_name5");
    tmsTestPlan.setEnvironmentId(4L);
    tmsTestPlan.setProductVersionId(4L);
    tmsTestPlan.setMilestoneIds(List.of(4L));
    tmsTestPlan.setAttributes(List.of(attribute));

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(tmsTestPlan);

    mockMvc.perform(put("/project/5/tms/test-plan/5")
                    .contentType("application/json")
                    .content(jsonContent)
                    .with(token(oAuthHelper.getSuperadminToken())))
                    .andExpect(status().isOk());

    Optional<TmsTestPlan> testPlan = testPlanRepository.findById(5L);

    assertTrue(testPlan.isPresent());
    assertEquals(tmsTestPlan.getName(), testPlan.get().getName());
    assertEquals(tmsTestPlan.getDescription(), testPlan.get().getDescription());
    assertEquals(tmsTestPlan.getEnvironmentId(), testPlan.get().getEnvironment().getId());
    TmsMilestone[] versionArray = testPlan.get().getMilestones()
            .toArray(new TmsMilestone[0]);
    assertEquals(tmsTestPlan.getMilestoneIds().get(0), versionArray[0].getId());
    assertEquals(tmsTestPlan.getProductVersionId(), testPlan.get().getProductVersion().getId());
  }

  @Test
  void getTestPlanByIdIntegrationTest() throws Exception {
    Optional<TmsTestPlan> testPlan = testPlanRepository.findById(5L);

    mockMvc.perform(get("/project/5/tms/test-plan/5")
            .with(token(oAuthHelper.getSuperadminToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(testPlan.get().getId()))
            .andExpect(jsonPath("$.name").value(testPlan.get().getName()))
            .andExpect(jsonPath("$.description").value(testPlan.get().getDescription()))
            .andExpect(jsonPath("$.environment.id").value(testPlan.get().getEnvironment().getId()))
            .andExpect(jsonPath("$.productVersion.id").value(testPlan.get()
                                                            .getProductVersion().getId()));
  }

  @Test
  void deleteTestPlanIntegrationTest() throws Exception {

    mockMvc.perform(delete("/project/6/tms/test-plan/6")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(token(oAuthHelper.getSuperadminToken())))
                    .andExpect(status().isOk());

    assertFalse(testPlanRepository.findById(6L).isPresent());
  }

  @Test
  void patchTestPlanTest() throws Exception {
    TmsTestPlanAttributeRQ attributQ = new TmsTestPlanAttributeRQ();
    attributQ.setValue("value5");
    attributQ.setAttributeId(5L);

    TmsTestPlanRQ tmsTestPlan = new TmsTestPlanRQ();
    tmsTestPlan.setName("updated_name5");
    tmsTestPlan.setDescription("updated_name5");
    tmsTestPlan.setEnvironmentId(4L);
    tmsTestPlan.setProductVersionId(4L);
    tmsTestPlan.setMilestoneIds(List.of(4L));
    tmsTestPlan.setAttributes(List.of(attributQ));

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(tmsTestPlan);

    mockMvc.perform(patch("/project/5/tms/test-plan/5")
                    .contentType("application/json")
                    .content(jsonContent)
                    .with(token(oAuthHelper.getSuperadminToken())))
                    .andExpect(status().isOk());

    Optional<TmsTestPlan> testPlan = testPlanRepository.findById(5L);

    assertTrue(testPlan.isPresent());
    assertEquals(tmsTestPlan.getName(), testPlan.get().getName());
    assertEquals(tmsTestPlan.getDescription(), testPlan.get().getDescription());
    assertEquals(tmsTestPlan.getEnvironmentId(), testPlan.get().getEnvironment().getId());
    TmsMilestone[] versionArray = testPlan.get().getMilestones()
            .toArray(new TmsMilestone[0]);
    assertEquals(tmsTestPlan.getMilestoneIds().get(0), versionArray[0].getId());
    assertEquals(tmsTestPlan.getProductVersionId(), testPlan.get().getProductVersion().getId());
  }
}
