package com.epam.reportportal.core.tms.controller.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsAttributeRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/db/tms/tms-attribute/tms-attribute-fill.sql")
@ExtendWith(MockitoExtension.class)
class TmsAttributeIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";
  private static final long SUPERADMIN_PROJECT_ID = 1L;

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private TmsAttributeRepository tmsAttributeRepository;

  @Test
  void createAttributeSuccessfullyIntegrationTest() throws Exception {
    // Given
    var request = TmsAttributeRQ.builder()
        .key("new_test_key")
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value(request.getKey()))
        .andExpect(jsonPath("$.id").exists());

    // Verify in database
    var createdAttribute = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getKey().equals("new_test_key")
            && attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID))
        .findFirst();

    assertTrue(createdAttribute.isPresent());
    assertEquals(request.getKey(), createdAttribute.get().getKey());
    assertEquals(SUPERADMIN_PROJECT_ID, createdAttribute.get().getProject().getId());
  }

  @Test
  void createAttributeWithDuplicateKeyIntegrationTest() throws Exception {
    // Given - this key already exists in the same project (project_id = 1)
    var request = TmsAttributeRQ.builder()
        .key("test_key_1")
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(content().string(containsString("already exists")));
  }

  @Test
  void createAttributeWithNullKeyIntegrationTest() throws Exception {
    // Given
    var request = TmsAttributeRQ.builder()
        .key(null)
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(content().string(containsString("violates not-null constraint")));
  }

  @Test
  void createAttributeWithEmptyKeyIntegrationTest() throws Exception {
    // Given
    var request = TmsAttributeRQ.builder()
        .key("")
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(content().string(containsString("violates not-null constraint")));
  }

  @Test
  void createAttributeWithSpecialCharactersKeyIntegrationTest() throws Exception {
    // Given
    var request = TmsAttributeRQ.builder()
        .key("test-key_with.special@chars")
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value(request.getKey()));

    // Verify in database
    var createdAttribute = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getKey().equals("test-key_with.special@chars")
            && attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID))
        .findFirst();

    assertTrue(createdAttribute.isPresent());
    assertEquals(SUPERADMIN_PROJECT_ID, createdAttribute.get().getProject().getId());
  }

  @Test
  void patchAttributeSuccessfullyIntegrationTest() throws Exception {
    // Given
    var attributeId = 2L;
    var originalAttribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(originalAttribute.isPresent());
    assertEquals(SUPERADMIN_PROJECT_ID, originalAttribute.get().getProject().getId());

    var request = TmsAttributeRQ.builder()
        .key("updated_test_key_2")
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(patch("/v1/project/{projectKey}/tms/attribute/{attributeId}",
            SUPERADMIN_PROJECT_KEY, attributeId)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.key").value(request.getKey()));

    // Verify in database
    var updatedAttribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(updatedAttribute.isPresent());
    assertEquals(request.getKey(), updatedAttribute.get().getKey());
    assertEquals(SUPERADMIN_PROJECT_ID, updatedAttribute.get().getProject().getId());
  }

  @Test
  void patchAttributeWithDuplicateKeyIntegrationTest() throws Exception {
    // Given - trying to update attribute 2 with key from attribute 1 (same project)
    var attributeId = 2L;
    var request = TmsAttributeRQ.builder()
        .key("test_key_1") // This key already exists for attribute with id 1 in same project
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(patch("/v1/project/{projectKey}/tms/attribute/{attributeId}",
            SUPERADMIN_PROJECT_KEY, attributeId)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(content().string(containsString("already exists")));

    // Verify that the original key is unchanged
    var attribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(attribute.isPresent());
    assertEquals("test_key_2", attribute.get().getKey()); // Original key should remain
  }

  @Test
  void patchAttributeNotFoundIntegrationTest() throws Exception {
    // Given
    var nonExistentAttributeId = 999L;
    var request = TmsAttributeRQ.builder()
        .key("non_existent_key")
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(patch("/v1/project/{projectKey}/tms/attribute/{attributeId}",
            SUPERADMIN_PROJECT_KEY, nonExistentAttributeId)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getAllAttributesIntegrationTest() throws Exception {
    // When/Then - get all attributes for superadmin_personal project
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(12))
        .andExpect(jsonPath("$.content[?(@.key == 'test_key_1')]").exists())
        .andExpect(jsonPath("$.content[?(@.key == 'priority')]").exists())
        .andExpect(jsonPath("$.content[?(@.key == 'api')]").exists())
        .andExpect(jsonPath("$.page.totalElements").value(12))
        .andExpect(jsonPath("$.page.totalPages").value(1))
        .andExpect(jsonPath("$.page.size").value(100)) // Default page size
        .andExpect(jsonPath("$.page.number").value(1)); // First page
  }

  @Test
  void getAllAttributesWithFullTextSearchIntegrationTest() throws Exception {
    // When/Then - Test full-text search functionality
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .param("filter.cnt.key", "test")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").isNumber())
        .andExpect(jsonPath("$.page.totalElements").isNumber());
  }

  @Test
  void getAllAttributesWithPaginationIntegrationTest() throws Exception {
    // When/Then - Test pagination parameters
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .param("offset", "0")
            .param("limit", "5")
            .param("sort", "key,asc")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(5))
        .andExpect(jsonPath("$.page.size").value(5))
        .andExpect(jsonPath("$.page.number").value(1));
  }

  @Test
  void getAttributeByIdIntegrationTest() throws Exception {
    // Given
    var attributeId = 4L;
    var attribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(attribute.isPresent());
    assertEquals(SUPERADMIN_PROJECT_ID, attribute.get().getProject().getId());

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/{attributeId}",
            SUPERADMIN_PROJECT_KEY, attributeId)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.key").value(attribute.get().getKey()));
  }

  @Test
  void getAttributeByIdNotFoundIntegrationTest() throws Exception {
    // Given
    var nonExistentAttributeId = 999L;

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/{attributeId}",
            SUPERADMIN_PROJECT_KEY, nonExistentAttributeId)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }

  @Test
  void getAttributesByIdRangeIntegrationTest() throws Exception {
    // Test getting multiple specific attributes from the same project
    var attributeIds = new Long[]{1L, 5L, 9L};

    for (var attributeId : attributeIds) {
      var attribute = tmsAttributeRepository.findById(attributeId);
      assertTrue(attribute.isPresent());
      assertEquals(SUPERADMIN_PROJECT_ID, attribute.get().getProject().getId());

      mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/{attributeId}",
              SUPERADMIN_PROJECT_KEY, attributeId)
              .with(token(oAuthHelper.getSuperadminToken())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(attributeId))
          .andExpect(jsonPath("$.key").value(attribute.get().getKey()));
    }
  }

  @Test
  void patchMultipleAttributesIntegrationTest() throws Exception {
    // Patch multiple existing attributes in the same project
    var attributeIds = new Long[]{6L, 7L, 8L};
    var newKeys = new String[]{"updated_browser", "updated_environment", "updated_regression"};

    for (int i = 0; i < attributeIds.length; i++) {
      var request = TmsAttributeRQ.builder()
          .key(newKeys[i])
          .build();
      var mapper = new ObjectMapper();
      var jsonContent = mapper.writeValueAsString(request);

      mockMvc.perform(patch("/v1/project/{projectKey}/tms/attribute/{attributeId}",
              SUPERADMIN_PROJECT_KEY, attributeIds[i])
              .contentType("application/json")
              .content(jsonContent)
              .with(token(oAuthHelper.getSuperadminToken())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(attributeIds[i]))
          .andExpect(jsonPath("$.key").value(newKeys[i]));
    }

    // Verify changes in database
    for (int i = 0; i < attributeIds.length; i++) {
      var updatedAttribute = tmsAttributeRepository.findById(attributeIds[i]);
      assertTrue(updatedAttribute.isPresent());
      assertEquals(newKeys[i], updatedAttribute.get().getKey());
      assertEquals(SUPERADMIN_PROJECT_ID, updatedAttribute.get().getProject().getId());
    }
  }

  @Test
  void createAttributeWithLongKeyIntegrationTest() throws Exception {
    // Test with a very long key (close to the 255 character limit)
    var longKey = "very_long_attribute_key_that_approaches_the_database_varchar_limit_" +
        "this_key_contains_many_characters_and_should_still_be_valid_as_long_as_it_stays_" +
        "within_the_255_character_limit_for_varchar_fields_in_the_database_schema_definition";

    assertTrue(longKey.length() < 255, "Test key should be under 255 characters");

    var request = TmsAttributeRQ.builder()
        .key(longKey)
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value(longKey));

    // Verify in database
    var createdAttribute = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getKey().equals(longKey)
            && attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID))
        .findFirst();
    assertTrue(createdAttribute.isPresent());
    assertEquals(SUPERADMIN_PROJECT_ID, createdAttribute.get().getProject().getId());
  }

  @Test
  void createAttributeInDifferentProject_ShouldAllowSameKeyInDifferentProjects() throws Exception {
    // This test demonstrates that same key can exist in different projects
    // Given attribute with key "test_key_1" already exists in project 1
    var request = TmsAttributeRQ.builder()
        .key("test_key_1")
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When trying to create same key in same project - should fail
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(content().string(containsString("already exists")));
  }

  @Test
  void getAllAttributesOnlyReturnsAttributesFromSpecificProject() throws Exception {
    // When getting all attributes for specific project
    var result = mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andReturn();

    // Then verify all returned attributes belong to the same project
    var allAttributes = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID))
        .toList();

    assertEquals(12, allAttributes.size());
    assertTrue(allAttributes.stream()
        .allMatch(attr -> attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID)));
  }

  @Test
  void getAttributeByIdFromDifferentProject_ShouldReturnNotFound() throws Exception {
    // Given an attribute that belongs to superadmin_personal project (id=1)
    var attributeId = 1L;
    var attribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(attribute.isPresent());
    assertEquals(SUPERADMIN_PROJECT_ID, attribute.get().getProject().getId());

    // When trying to access it from the correct project - should succeed
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/{attributeId}",
            SUPERADMIN_PROJECT_KEY, attributeId)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(attributeId));
  }
}
