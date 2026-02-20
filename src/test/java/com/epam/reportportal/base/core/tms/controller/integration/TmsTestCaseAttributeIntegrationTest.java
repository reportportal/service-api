
package com.epam.reportportal.base.core.tms.controller.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.tms.dto.GetAttributesByTestCaseIdsRQ;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

/**
 * Integration tests for TmsTestCaseAttributeController.
 * <p>
 * Test data setup (tms-test-case-attribute-fill.sql):
 * <ul>
 *   <li>TC1 (project 1): browser=chrome (1), priority=high (3)</li>
 *   <li>TC2 (project 1): browser=chrome (1), browser=firefox (2), environment=staging (5)</li>
 *   <li>TC3 (project 1): priority=low (4), os=linux (6)</li>
 *   <li>TC4 (project 1): no attributes</li>
 *   <li>TC5 (project 1): browser=chrome (1), priority=high (3), environment=staging (5)</li>
 *   <li>TC6 (project 2): browser=safari (7), priority=medium (8)</li>
 * </ul>
 */
@Sql("/db/tms/tms-test-case-attribute/tms-test-case-attribute-fill.sql")
class TmsTestCaseAttributeIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";
  private static final String BASE_URL =
      "/v1/project/" + SUPERADMIN_PROJECT_KEY + "/tms/test-case/attribute";

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void getAttributesByTestCaseIds_ShouldReturnUniqueAttributes() throws Exception {
    // Given - TC1 has attrs {1,3}, TC2 has attrs {1,2,5} → distinct = {1,2,3,5}
    var request = new GetAttributesByTestCaseIdsRQ();
    request.setTestCaseIds(List.of(1L, 2L));

    // When/Then
    mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(4))
        .andExpect(jsonPath("$.page.totalElements").value(4));
  }

  @Test
  void getAttributesByTestCaseIds_ShouldReturnDistinctAttributes_WhenTestCasesShareSameAttributes()
      throws Exception {
    // Given - TC1 has {1,3}, TC5 has {1,3,5} → distinct = {1,3,5}
    var request = new GetAttributesByTestCaseIdsRQ();
    request.setTestCaseIds(List.of(1L, 5L));

    // When/Then
    mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(3))
        .andExpect(jsonPath("$.page.totalElements").value(3));
  }

  @Test
  void getAttributesByTestCaseIds_ShouldReturnEmptyPage_WhenTestCaseHasNoAttributes()
      throws Exception {
    // Given - TC4 has no attributes
    var request = new GetAttributesByTestCaseIdsRQ();
    request.setTestCaseIds(List.of(4L));

    // When/Then
    mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(0));
  }

  @Test
  void getAttributesByTestCaseIds_ShouldReturnEmptyPage_WhenTestCaseIdsDoNotExist()
      throws Exception {
    // Given - non-existent test case IDs
    var request = new GetAttributesByTestCaseIdsRQ();
    request.setTestCaseIds(List.of(999L, 1000L));

    // When/Then
    mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(0));
  }

  @Test
  void getAttributesByTestCaseIds_ShouldReturnAttributes_ForSingleTestCase() throws Exception {
    // Given - TC3 has attrs {4,6} (priority=low, os=linux)
    var request = new GetAttributesByTestCaseIdsRQ();
    request.setTestCaseIds(List.of(3L));

    // When/Then
    mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(2));
  }

  @Test
  void getAttributesByTestCaseIds_ShouldReturnAllProjectAttributes_WhenAllTestCasesIncluded()
      throws Exception {
    // Given - TC1{1,3}, TC2{1,2,5}, TC3{4,6}, TC5{1,3,5} → distinct = {1,2,3,4,5,6}
    var request = new GetAttributesByTestCaseIdsRQ();
    request.setTestCaseIds(List.of(1L, 2L, 3L, 5L));

    // When/Then
    mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(6))
        .andExpect(jsonPath("$.page.totalElements").value(6));
  }

  @Test
  void getAttributesByTestCaseIds_ShouldNotReturnAttributesFromOtherProject() throws Exception {
    // Given - TC6 is in project 2, request is for project 1
    // TC6 has attrs {7,8} which belong to project 2
    var request = new GetAttributesByTestCaseIdsRQ();
    request.setTestCaseIds(List.of(6L));

    // When/Then - should return empty because TC6's folder belongs to project 2,
    // and the repository filters by project_id = 1 (superadmin_personal)
    mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(0));
  }

  @Test
  void getAttributesByTestCaseIds_ShouldSupportPagination() throws Exception {
    // Given - TC1{1,3}, TC2{1,2,5}, TC3{4,6} → distinct = {1,2,3,4,5,6} = 6 total
    var request = new GetAttributesByTestCaseIdsRQ();
    request.setTestCaseIds(List.of(1L, 2L, 3L));

    // When/Then - request limit = 2, offset = 0
    mockMvc.perform(post(BASE_URL)
            .param("limit", "2")
            .param("offset", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(6))
        .andExpect(jsonPath("$.page.size").value(2))
        .andExpect(jsonPath("$.page.number").value(1));
  }

  @Test
  void getAttributesByTestCaseIds_ShouldReturnAttributeFields() throws Exception {
    // Given - TC3 has {priority=low (id=4), os=linux (id=6)}
    var request = new GetAttributesByTestCaseIdsRQ();
    request.setTestCaseIds(List.of(3L));

    // When/Then - verify all fields are present in response
    mockMvc.perform(post(BASE_URL)
            .param("page.sort", "id,ASC")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").exists())
        .andExpect(jsonPath("$.content[0].key").exists())
        .andExpect(jsonPath("$.content[0].value").exists());
  }

  @Test
  void getAttributesByTestCaseIds_ShouldReturnBadRequest_WhenTestCaseIdsIsEmpty()
      throws Exception {
    // Given - empty testCaseIds list
    var request = new GetAttributesByTestCaseIdsRQ();
    request.setTestCaseIds(Collections.emptyList());

    // When/Then
    mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAttributesByTestCaseIds_ShouldReturnBadRequest_WhenTestCaseIdsIsNull()
      throws Exception {
    // Given - null testCaseIds
    String jsonContent = "{}";

    // When/Then
    mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAttributesByTestCaseIds_ShouldReturnBadRequest_WhenMalformedJson() throws Exception {
    // Given - malformed JSON
    String malformedJson = "{\"testCaseIds\": [1, 2";

    // When/Then
    mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(malformedJson)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAttributesByTestCaseIds_ShouldHandleMixOfExistingAndNonExistingIds() throws Exception {
    // Given - TC1 exists (has attrs {1,3}), TC999 does not exist
    var request = new GetAttributesByTestCaseIdsRQ();
    request.setTestCaseIds(List.of(1L, 999L));

    // When/Then - should return only attributes from existing test cases
    mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(2));
  }

  @Test
  void getAttributesByTestCaseIds_ShouldHandleMixOfWithAndWithoutAttributes() throws Exception {
    // Given - TC1 has attrs {1,3}, TC4 has no attributes → distinct = {1,3}
    var request = new GetAttributesByTestCaseIdsRQ();
    request.setTestCaseIds(List.of(1L, 4L));

    // When/Then
    mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(2));
  }
}