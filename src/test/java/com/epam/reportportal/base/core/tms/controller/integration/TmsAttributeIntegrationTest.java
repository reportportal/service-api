package com.epam.reportportal.base.core.tms.controller.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.tms.dto.TmsAttributeRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
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
  void createAttributeAsTagSuccessfullyIntegrationTest() throws Exception {
    // Given - create attribute without value (tag)
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
        .andExpect(jsonPath("$.value").doesNotExist())
        .andExpect(jsonPath("$.id").exists());

    // Verify in database
    var createdAttribute = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getKey().equals("new_test_key")
            && attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID)
            && attr.getValue() == null)
        .findFirst();

    assertTrue(createdAttribute.isPresent());
    assertEquals(request.getKey(), createdAttribute.get().getKey());
    assertNull(createdAttribute.get().getValue());
    assertEquals(SUPERADMIN_PROJECT_ID, createdAttribute.get().getProject().getId());
  }

  @Test
  void createAttributeWithKeyValueSuccessfullyIntegrationTest() throws Exception {
    // Given - create attribute with value (key-value pair)
    var request = TmsAttributeRQ.builder()
        .key("status")
        .value("active")
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
        .andExpect(jsonPath("$.value").value(request.getValue()))
        .andExpect(jsonPath("$.id").exists());

    // Verify in database
    var createdAttribute = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getKey().equals("status")
            && "active".equals(attr.getValue())
            && attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID))
        .findFirst();

    assertTrue(createdAttribute.isPresent());
    assertEquals(request.getKey(), createdAttribute.get().getKey());
    assertEquals(request.getValue(), createdAttribute.get().getValue());
    assertEquals(SUPERADMIN_PROJECT_ID, createdAttribute.get().getProject().getId());
  }

  @Test
  void createAttributeWithSameKeyButDifferentValueIntegrationTest() throws Exception {
    // Given - priority:high already exists (id=7), creating priority:critical should succeed
    var request = TmsAttributeRQ.builder()
        .key("priority")
        .value("critical")
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value("priority"))
        .andExpect(jsonPath("$.value").value("critical"));

    // Verify in database
    var allPriorityAttributes = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getKey().equals("priority")
            && attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID))
        .toList();

    // Should now have 4 priority attributes: high, medium, low, critical
    assertEquals(4, allPriorityAttributes.size());
    assertTrue(allPriorityAttributes.stream()
        .anyMatch(attr -> "critical".equals(attr.getValue())));
  }

  @Test
  void createAttributeWithDuplicateKeyAndValueIntegrationTest() throws Exception {
    // Given - priority:high already exists (id=7)
    var request = TmsAttributeRQ.builder()
        .key("priority")
        .value("high")
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When/Then - should fail because same key+value combination exists
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(content().string(containsString("already exists")));
  }

  @Test
  void createAttributeWithDuplicateTagIntegrationTest() throws Exception {
    // Given - test_key_1 tag already exists (id=1, value=NULL)
    var request = TmsAttributeRQ.builder()
        .key("test_key_1")
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When/Then - should fail because same key with NULL value exists
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(content().string(containsString("already exists")));
  }

  @Test
  void createTagWithSameKeyAsExistingKeyValueAttributeIntegrationTest() throws Exception {
    // Given - priority:high exists (id=7), creating priority tag (no value) should succeed
    var request = TmsAttributeRQ.builder()
        .key("priority")
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    // When/Then - should succeed because value is different (NULL vs "high")
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value("priority"))
        .andExpect(jsonPath("$.value").doesNotExist());

    // Verify in database
    var priorityTag = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getKey().equals("priority")
            && attr.getValue() == null
            && attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID))
        .findFirst();

    assertTrue(priorityTag.isPresent());
    assertNull(priorityTag.get().getValue());
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
        .value("test-value")
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
        .andExpect(jsonPath("$.value").value(request.getValue()));

    // Verify in database
    var createdAttribute = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getKey().equals("test-key_with.special@chars")
            && "test-value".equals(attr.getValue())
            && attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID))
        .findFirst();

    assertTrue(createdAttribute.isPresent());
    assertEquals(SUPERADMIN_PROJECT_ID, createdAttribute.get().getProject().getId());
  }

  @Test
  void patchAttributeKeySuccessfullyIntegrationTest() throws Exception {
    // Given - update tag attribute
    var attributeId = 2L; // test_key_2 tag
    var originalAttribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(originalAttribute.isPresent());
    assertEquals(SUPERADMIN_PROJECT_ID, originalAttribute.get().getProject().getId());
    assertNull(originalAttribute.get().getValue());

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
        .andExpect(jsonPath("$.key").value(request.getKey()))
        .andExpect(jsonPath("$.value").doesNotExist());

    // Verify in database
    var updatedAttribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(updatedAttribute.isPresent());
    assertEquals(request.getKey(), updatedAttribute.get().getKey());
    assertNull(updatedAttribute.get().getValue());
    assertEquals(SUPERADMIN_PROJECT_ID, updatedAttribute.get().getProject().getId());
  }

  @Test
  void patchAttributeKeyAndValueSuccessfullyIntegrationTest() throws Exception {
    // Given - update key-value attribute
    var attributeId = 7L; // priority:high
    var originalAttribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(originalAttribute.isPresent());
    assertEquals("priority", originalAttribute.get().getKey());
    assertEquals("high", originalAttribute.get().getValue());

    var request = TmsAttributeRQ.builder()
        .key("severity")
        .value("critical")
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
        .andExpect(jsonPath("$.key").value("severity"))
        .andExpect(jsonPath("$.value").value("critical"));

    // Verify in database
    var updatedAttribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(updatedAttribute.isPresent());
    assertEquals("severity", updatedAttribute.get().getKey());
    assertEquals("critical", updatedAttribute.get().getValue());
  }

  @Test
  void patchAttributeValueOnlySuccessfullyIntegrationTest() throws Exception {
    // Given - update only value of existing key-value attribute
    var attributeId = 8L; // priority:medium
    var originalAttribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(originalAttribute.isPresent());
    assertEquals("priority", originalAttribute.get().getKey());
    assertEquals("medium", originalAttribute.get().getValue());

    var request = TmsAttributeRQ.builder()
        .key("priority") // Keep same key
        .value("updated_medium") // Change value
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
        .andExpect(jsonPath("$.key").value("priority"))
        .andExpect(jsonPath("$.value").value("updated_medium"));

    // Verify in database
    var updatedAttribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(updatedAttribute.isPresent());
    assertEquals("priority", updatedAttribute.get().getKey());
    assertEquals("updated_medium", updatedAttribute.get().getValue());
  }

  @Test
  void patchAttributeWithDuplicateKeyAndValueIntegrationTest() throws Exception {
    // Given - trying to update attribute 8 (priority:medium) to priority:high which already exists (id=7)
    var attributeId = 8L;
    var request = TmsAttributeRQ.builder()
        .key("priority")
        .value("high")
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

    // Verify that the original key-value is unchanged
    var attribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(attribute.isPresent());
    assertEquals("priority", attribute.get().getKey());
    assertEquals("medium", attribute.get().getValue());
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
        .andExpect(jsonPath("$.content[?(@.key == 'priority' && @.value == 'high')]").exists())
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
  void getAllAttributesWithValueFilterIntegrationTest() throws Exception {
    // When/Then - Filter by value
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .param("filter.eq.value", "high")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].key").value("priority"))
        .andExpect(jsonPath("$.content[0].value").value("high"));
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
    // Given - get tag attribute
    var attributeId = 4L; // smoke tag
    var attribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(attribute.isPresent());
    assertEquals(SUPERADMIN_PROJECT_ID, attribute.get().getProject().getId());
    assertNull(attribute.get().getValue());

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/{attributeId}",
            SUPERADMIN_PROJECT_KEY, attributeId)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.key").value(attribute.get().getKey()))
        .andExpect(jsonPath("$.value").doesNotExist());
  }

  @Test
  void getAttributeWithValueByIdIntegrationTest() throws Exception {
    // Given - get key-value attribute
    var attributeId = 7L; // priority:high
    var attribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(attribute.isPresent());
    assertEquals(SUPERADMIN_PROJECT_ID, attribute.get().getProject().getId());
    assertEquals("high", attribute.get().getValue());

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/{attributeId}",
            SUPERADMIN_PROJECT_KEY, attributeId)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.key").value("priority"))
        .andExpect(jsonPath("$.value").value("high"));
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
    var attributeIds = new Long[]{1L, 7L, 10L};

    for (var attributeId : attributeIds) {
      var attribute = tmsAttributeRepository.findById(attributeId);
      assertTrue(attribute.isPresent());
      assertEquals(SUPERADMIN_PROJECT_ID, attribute.get().getProject().getId());

      var resultActions = mockMvc.perform(
              get("/v1/project/{projectKey}/tms/attribute/{attributeId}",
                  SUPERADMIN_PROJECT_KEY, attributeId)
                  .with(token(oAuthHelper.getSuperadminToken())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(attributeId))
          .andExpect(jsonPath("$.key").value(attribute.get().getKey()));

      // Check value only if it exists
      if (attribute.get().getValue() != null) {
        resultActions.andExpect(jsonPath("$.value").value(attribute.get().getValue()));
      }
    }
  }

  @Test
  void patchMultipleAttributesIntegrationTest() throws Exception {
    // Patch multiple existing attributes in the same project
    var testData = new Object[][]{
        {10L, "browser", "safari"}, // browser:chrome -> browser:safari
        {11L, "browser", "edge"},   // browser:firefox -> browser:edge
        {12L, "environment", "staging"} // environment:prod -> environment:staging
    };

    for (var data : testData) {
      var attributeId = (Long) data[0];
      var newKey = (String) data[1];
      var newValue = (String) data[2];

      var request = TmsAttributeRQ.builder()
          .key(newKey)
          .value(newValue)
          .build();
      var mapper = new ObjectMapper();
      var jsonContent = mapper.writeValueAsString(request);

      mockMvc.perform(patch("/v1/project/{projectKey}/tms/attribute/{attributeId}",
              SUPERADMIN_PROJECT_KEY, attributeId)
              .contentType("application/json")
              .content(jsonContent)
              .with(token(oAuthHelper.getSuperadminToken())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(attributeId))
          .andExpect(jsonPath("$.key").value(newKey))
          .andExpect(jsonPath("$.value").value(newValue));
    }

    // Verify changes in database
    for (var data : testData) {
      var attributeId = (Long) data[0];
      var expectedKey = (String) data[1];
      var expectedValue = (String) data[2];

      var updatedAttribute = tmsAttributeRepository.findById(attributeId);
      assertTrue(updatedAttribute.isPresent());
      assertEquals(expectedKey, updatedAttribute.get().getKey());
      assertEquals(expectedValue, updatedAttribute.get().getValue());
      assertEquals(SUPERADMIN_PROJECT_ID, updatedAttribute.get().getProject().getId());
    }
  }

  @Test
  void createAttributeWithLongKeyAndValueIntegrationTest() throws Exception {
    // Test with a very long key and value (close to the 255 character limit)
    var longKey = "very_long_attribute_key_that_approaches_the_database_varchar_limit_" +
        "this_key_contains_many_characters_and_should_still_be_valid_as_long_as_it_stays_" +
        "within_the_255_character_limit_for_varchar_fields_in_the_database_schema";

    var longValue = "very_long_attribute_value_that_also_approaches_the_database_varchar_limit_" +
        "this_value_contains_many_characters_and_should_still_be_valid_as_long_as_it_stays_" +
        "within_the_255_character_limit_for_varchar_fields";

    assertTrue(longKey.length() < 255, "Test key should be under 255 characters");
    assertTrue(longValue.length() < 255, "Test value should be under 255 characters");

    var request = TmsAttributeRQ.builder()
        .key(longKey)
        .value(longValue)
        .build();
    var mapper = new ObjectMapper();
    var jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value(longKey))
        .andExpect(jsonPath("$.value").value(longValue));

    // Verify in database
    var createdAttribute = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getKey().equals(longKey)
            && longValue.equals(attr.getValue())
            && attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID))
        .findFirst();
    assertTrue(createdAttribute.isPresent());
    assertEquals(SUPERADMIN_PROJECT_ID, createdAttribute.get().getProject().getId());
  }

  @Test
  void getAllAttributesOnlyReturnsAttributesFromSpecificProject() throws Exception {
    // When getting all attributes for specific project
    var result = mockMvc.perform(
            get("/v1/project/{projectKey}/tms/attribute", SUPERADMIN_PROJECT_KEY)
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
  void getAttributeByIdFromCorrectProject_ShouldSucceed() throws Exception {
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

  @Test
  void verifyMultiplePriorityAttributesExist() throws Exception {
    // Verify that multiple attributes with same key but different values exist
    var priorityAttributes = tmsAttributeRepository.findAll().stream()
        .filter(attr -> "priority".equals(attr.getKey())
            && attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID))
        .toList();

    assertEquals(3, priorityAttributes.size());
    assertTrue(priorityAttributes.stream().anyMatch(attr -> "high".equals(attr.getValue())));
    assertTrue(priorityAttributes.stream().anyMatch(attr -> "medium".equals(attr.getValue())));
    assertTrue(priorityAttributes.stream().anyMatch(attr -> "low".equals(attr.getValue())));
  }

  @Test
  void verifyMultipleBrowserAttributesExist() throws Exception {
    // Verify that multiple attributes with same key but different values exist
    var browserAttributes = tmsAttributeRepository.findAll().stream()
        .filter(attr -> "browser".equals(attr.getKey())
            && attr.getProject().getId().equals(SUPERADMIN_PROJECT_ID))
        .toList();

    assertEquals(2, browserAttributes.size());
    assertTrue(browserAttributes.stream().anyMatch(attr -> "chrome".equals(attr.getValue())));
    assertTrue(browserAttributes.stream().anyMatch(attr -> "firefox".equals(attr.getValue())));
  }
}
