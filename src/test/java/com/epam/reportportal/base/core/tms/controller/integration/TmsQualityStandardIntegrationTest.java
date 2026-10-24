package com.epam.reportportal.base.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardCriterionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsQualityStandardRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/db/tms/tms-quality-standard/tms-quality-standard-fill.sql")
class TmsQualityStandardIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";
  private static final String DEFAULT_PROJECT_KEY = "default_personal";

  @Autowired
  private TmsQualityStandardRepository tmsQualityStandardRepository;

  private final ObjectMapper mapper = new ObjectMapper();

  private TmsQualityStandardRQ validRq() {
    return TmsQualityStandardRQ.builder()
        .name("New rubric")
        .description("Freshly created")
        .criteria(List.of(
            TmsQualityStandardCriterionRQ.builder().name("Correctness").maxPoints(70).sequence(1).build(),
            TmsQualityStandardCriterionRQ.builder().name("Clarity").maxPoints(30).sequence(2).build()))
        .build();
  }

  @Test
  void getStandard_WhenExists_ShouldReturnItWithCriteria() throws Exception {
    mockMvc.perform(get("/v1/project/" + DEFAULT_PROJECT_KEY + "/tms/quality-standard")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(900))
        .andExpect(jsonPath("$.name").value("Default TC quality rubric"))
        .andExpect(jsonPath("$.criteria.length()").value(2))
        .andExpect(jsonPath("$.criteria[0].name").value("Scenario correctness"))
        .andExpect(jsonPath("$.criteria[0].maxPoints").value(60));
  }

  @Test
  void getStandard_WhenNotFound_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/quality-standard")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void createStandard_WhenNoneExists_ShouldPersistAndReturnCreated() throws Exception {
    var rq = validRq();

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/quality-standard")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("New rubric"))
        .andExpect(jsonPath("$.criteria.length()").value(2));

    var created = tmsQualityStandardRepository.findByProjectId(1L);
    assertTrue(created.isPresent());
    assertEquals("New rubric", created.get().getName());
    assertEquals(2, created.get().getCriteria().size());
  }

  @Test
  void createStandard_WhenAlreadyExists_ShouldReturnConflictAndNotModifyExisting() throws Exception {
    var rq = validRq();

    mockMvc.perform(post("/v1/project/" + DEFAULT_PROJECT_KEY + "/tms/quality-standard")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isConflict());

    var existing = tmsQualityStandardRepository.findByProjectId(2L).orElseThrow();
    assertEquals("Default TC quality rubric", existing.getName());
  }

  @Test
  void createStandard_WithCriteriaNotSummingToOneHundred_ShouldReturnBadRequest() throws Exception {
    var rq = TmsQualityStandardRQ.builder()
        .name("Bad rubric")
        .criteria(List.of(TmsQualityStandardCriterionRQ.builder().name("c1").maxPoints(50).sequence(1).build()))
        .build();

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/quality-standard")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());

    assertFalse(tmsQualityStandardRepository.findByProjectId(1L).isPresent());
  }

  @Test
  void updateStandard_WhenExists_ShouldMergeCriteriaAndPersist() throws Exception {
    var rq = TmsQualityStandardRQ.builder()
        .name("Updated rubric")
        .description("Updated")
        .criteria(List.of(
            TmsQualityStandardCriterionRQ.builder().id(900L).name("Scenario correctness").maxPoints(80).sequence(1).build(),
            TmsQualityStandardCriterionRQ.builder().name("New criterion").maxPoints(20).sequence(2).build()))
        .build();

    mockMvc.perform(put("/v1/project/" + DEFAULT_PROJECT_KEY + "/tms/quality-standard")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated rubric"))
        .andExpect(jsonPath("$.criteria.length()").value(2));

    var updated = tmsQualityStandardRepository.findByProjectId(2L).orElseThrow();
    assertEquals("Updated rubric", updated.getName());
    assertEquals(2, updated.getCriteria().size());
    // Criterion 900 kept its id (merged in place), the removed criterion 901 is gone.
    assertTrue(updated.getCriteria().stream().anyMatch(c -> c.getId().equals(900L) && c.getMaxPoints() == 80));
    assertFalse(updated.getCriteria().stream().anyMatch(c -> c.getId().equals(901L)));
  }

  @Test
  void updateStandard_WhenNotFound_ShouldReturnNotFound() throws Exception {
    var rq = validRq();

    mockMvc.perform(put("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/quality-standard")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteStandard_WhenExists_ShouldRemoveItAndItsCriteria() throws Exception {
    mockMvc.perform(delete("/v1/project/" + DEFAULT_PROJECT_KEY + "/tms/quality-standard")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    assertFalse(tmsQualityStandardRepository.findByProjectId(2L).isPresent());
  }
}
