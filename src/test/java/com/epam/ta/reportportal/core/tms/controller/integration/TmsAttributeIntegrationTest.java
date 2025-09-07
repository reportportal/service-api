package com.epam.ta.reportportal.core.tms.controller.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.core.tms.db.entity.TmsAttribute;
import com.epam.ta.reportportal.core.tms.db.repository.TmsAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
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

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private TmsAttributeRepository tmsAttributeRepository;

  @Test
  void createAttributeSuccessfullyIntegrationTest() throws Exception {
    // Given
    TmsAttributeRQ request = TmsAttributeRQ.builder()
        .key("new_test_key")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(post("/v1/tms/attribute")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value(request.getKey()))
        .andExpect(jsonPath("$.id").exists());

    // Verify in database
    var createdAttribute = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getKey().equals("new_test_key"))
        .findFirst();

    assertTrue(createdAttribute.isPresent());
    assertEquals(request.getKey(), createdAttribute.get().getKey());
  }

  @Test
  void createAttributeWithDuplicateKeyIntegrationTest() throws Exception {
    // Given
    TmsAttributeRQ request = TmsAttributeRQ.builder()
        .key("test_key_1") // This key already exists in test data
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(post("/v1/tms/attribute")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(content().string(containsString("already exists")));
  }

  @Test
  void createAttributeWithNullKeyIntegrationTest() throws Exception {
    // Given
    TmsAttributeRQ request = TmsAttributeRQ.builder()
        .key(null)
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(post("/v1/tms/attribute")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(content().string(containsString("violates not-null constraint")));
  }

  @Test
  void createAttributeWithEmptyKeyIntegrationTest() throws Exception {
    // Given
    TmsAttributeRQ request = TmsAttributeRQ.builder()
        .key("")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(post("/v1/tms/attribute")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(content().string(containsString("violates not-null constraint")));
  }

  @Test
  void createAttributeWithSpecialCharactersKeyIntegrationTest() throws Exception {
    // Given
    TmsAttributeRQ request = TmsAttributeRQ.builder()
        .key("test-key_with.special@chars")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(post("/v1/tms/attribute")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value(request.getKey()));

    // Verify in database
    var createdAttribute = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getKey().equals("test-key_with.special@chars"))
        .findFirst();

    assertTrue(createdAttribute.isPresent());
  }

  @Test
  void patchAttributeSuccessfullyIntegrationTest() throws Exception {
    // Given
    Long attributeId = 2L;
    Optional<TmsAttribute> originalAttribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(originalAttribute.isPresent());

    var request = TmsAttributeRQ.builder()
        .key("updated_test_key_2")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(patch("/v1/tms/attribute/{attributeId}", attributeId)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.key").value(request.getKey()));

    // Verify in database
    Optional<TmsAttribute> updatedAttribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(updatedAttribute.isPresent());
    assertEquals(request.getKey(), updatedAttribute.get().getKey());
  }

  @Test
  void patchAttributeWithDuplicateKeyIntegrationTest() throws Exception {
    // Given
    Long attributeId = 2L;
    TmsAttributeRQ request = TmsAttributeRQ.builder()
        .key("test_key_1") // This key already exists for attribute with id 1
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(patch("/v1/tms/attribute/{attributeId}", attributeId)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(content().string(containsString("already exists")));

    // Verify that the original key is unchanged
    Optional<TmsAttribute> attribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(attribute.isPresent());
    assertEquals("test_key_2", attribute.get().getKey()); // Original key should remain
  }

  @Test
  void patchAttributeNotFoundIntegrationTest() throws Exception {
    // Given
    Long nonExistentAttributeId = 999L;
    TmsAttributeRQ request = TmsAttributeRQ.builder()
        .key("non_existent_key")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    // When/Then
    mockMvc.perform(patch("/v1/tms/attribute/{attributeId}", nonExistentAttributeId)
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("TMS Attribute with id '999' not found")));
  }

  @Test
  void getAllAttributesIntegrationTest() throws Exception {
    // When/Then
    mockMvc.perform(get("/v1/tms/attribute")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(12))
        .andExpect(jsonPath("$.content[?(@.key == 'test_key_1')]").exists())
        .andExpect(jsonPath("$.content[?(@.key == 'priority')]").exists())
        .andExpect(jsonPath("$.content[?(@.key == 'api')]").exists())
        .andExpect(jsonPath("$.page.totalElements").value(12))
        .andExpect(jsonPath("$.page.totalPages").value(1))
        .andExpect(jsonPath("$.page.size").value(50)) // Default page size
        .andExpect(jsonPath("$.page.number").value(1)); // First page
  }

  @Test
  void getAttributeByIdIntegrationTest() throws Exception {
    // Given
    Long attributeId = 4L;
    Optional<TmsAttribute> attribute = tmsAttributeRepository.findById(attributeId);
    assertTrue(attribute.isPresent());

    // When/Then
    mockMvc.perform(get("/v1/tms/attribute/{attributeId}", attributeId)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.key").value(attribute.get().getKey()));
  }

  @Test
  void getAttributeByIdNotFoundIntegrationTest() throws Exception {
    // Given
    Long nonExistentAttributeId = 999L;

    // When/Then
    mockMvc.perform(get("/v1/tms/attribute/{attributeId}", nonExistentAttributeId)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("TMS Attribute with id '999' not found")));
  }

  @Test
  void getAttributesByIdRangeIntegrationTest() throws Exception {
    // Test getting multiple specific attributes
    Long[] attributeIds = {1L, 5L, 9L};

    for (Long attributeId : attributeIds) {
      Optional<TmsAttribute> attribute = tmsAttributeRepository.findById(attributeId);
      assertTrue(attribute.isPresent());

      mockMvc.perform(get("/v1/tms/attribute/{attributeId}", attributeId)
              .with(token(oAuthHelper.getSuperadminToken())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(attributeId))
          .andExpect(jsonPath("$.key").value(attribute.get().getKey()));
    }
  }

  @Test
  void patchMultipleAttributesIntegrationTest() throws Exception {
    // Patch multiple existing attributes
    Long[] attributeIds = {6L, 7L, 8L};
    String[] newKeys = {"updated_browser", "updated_environment", "updated_regression"};

    for (int i = 0; i < attributeIds.length; i++) {
      TmsAttributeRQ request = TmsAttributeRQ.builder()
          .key(newKeys[i])
          .build();
      ObjectMapper mapper = new ObjectMapper();
      String jsonContent = mapper.writeValueAsString(request);

      mockMvc.perform(patch("/v1/tms/attribute/{attributeId}", attributeIds[i])
              .contentType("application/json")
              .content(jsonContent)
              .with(token(oAuthHelper.getSuperadminToken())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(attributeIds[i]))
          .andExpect(jsonPath("$.key").value(newKeys[i]));
    }

    // Verify changes in database
    for (int i = 0; i < attributeIds.length; i++) {
      Optional<TmsAttribute> updatedAttribute = tmsAttributeRepository.findById(attributeIds[i]);
      assertTrue(updatedAttribute.isPresent());
      assertEquals(newKeys[i], updatedAttribute.get().getKey());
    }
  }

  @Test
  void createAttributeWithLongKeyIntegrationTest() throws Exception {
    // Test with a very long key (close to the 255 character limit)
    String longKey = "very_long_attribute_key_that_approaches_the_database_varchar_limit_" +
        "this_key_contains_many_characters_and_should_still_be_valid_as_long_as_it_stays_" +
        "within_the_255_character_limit_for_varchar_fields_in_the_database_schema_definition";

    assertTrue(longKey.length() < 255, "Test key should be under 255 characters");

    TmsAttributeRQ request = TmsAttributeRQ.builder()
        .key(longKey)
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(post("/v1/tms/attribute")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value(longKey));

    // Verify in database
    var createdAttribute = tmsAttributeRepository.findAll().stream()
        .filter(attr -> attr.getKey().equals(longKey))
        .findFirst();
    assertTrue(createdAttribute.isPresent());
  }
}
