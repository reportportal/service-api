/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.integration.plugin.strategy.JsonPluginUploader;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.JsonSchemaValidator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Test class for {@link JsonPluginUploader}.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
public class JsonPluginUploaderTest {

  @Mock
  private IntegrationTypeRepository integrationTypeRepository;

  //  @Autowired
  @Mock
  private JsonSchemaValidator jsonSchemaValidator;

  //  @Autowired
  @Spy
  private JsonMapper objectMapper;

  private static final String SCHEMA_SOURCE = "https://schema.reportportal.io/";
  private static final String VALID_SCHEMA = SCHEMA_SOURCE + "manifest.schema.json";

  private JsonPluginUploader jsonPluginUploader;

  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
    jsonPluginUploader = new JsonPluginUploader(integrationTypeRepository, objectMapper, jsonSchemaValidator,
        SCHEMA_SOURCE);
  }

  @AfterEach
  void tearDown() throws Exception {
    closeable.close();
  }

  @Test
  void uploadPlugin() throws IOException {
    Map<String, Object> manifestMap = new HashMap<>();
    manifestMap.put("$schema", VALID_SCHEMA);
    manifestMap.put("id", "test-plugin");
    manifestMap.put("group", "BTS");
    String manifestJson = objectMapper.writeValueAsString(manifestMap);
    InputStream manifestStream = new ByteArrayInputStream(manifestJson.getBytes());

    IntegrationType mockIntegrationType = new IntegrationType();
    when(integrationTypeRepository.findByName(any())).thenReturn(Optional.empty());
    when(integrationTypeRepository.saveAndFlush(any(IntegrationType.class))).thenReturn(mockIntegrationType);
    when(jsonSchemaValidator.validate(any(String.class), any(JsonNode.class))).thenReturn(Collections.emptyList());

    IntegrationType result = jsonPluginUploader.uploadPlugin("manifest.json", manifestStream);

    assertNotNull(result, "Resulting IntegrationType should not be null");
    verify(integrationTypeRepository).findByName(any());
    verify(integrationTypeRepository).saveAndFlush(any(IntegrationType.class));
    verify(jsonSchemaValidator).validate(any(String.class), any(JsonNode.class));
    verify(integrationTypeRepository).saveAndFlush(argThat(type ->
        type.getIntegrationGroup().name().equals("BTS")));
  }

  @Test
  void uploadPluginWithEmptyId() throws IOException {
    Map<String, Object> manifestMap = new HashMap<>();
    manifestMap.put("$schema", VALID_SCHEMA);
    String manifestWithoutId = objectMapper.writeValueAsString(manifestMap);
    InputStream manifestStream = new ByteArrayInputStream(manifestWithoutId.getBytes());

    when(jsonSchemaValidator.validate(any(String.class), any(JsonNode.class)))
        .thenReturn(Collections.emptyList());

    assertThrows(ReportPortalException.class, () ->
        jsonPluginUploader.uploadPlugin("missing-id.json", manifestStream));
  }

  @Test
  void uploadPluginWithMissingSchemaLocation() {
    String manifestWithoutSchema = "{ \"id\": \"test-plugin\" }";
    InputStream manifestStream = new ByteArrayInputStream(manifestWithoutSchema.getBytes());

    assertThrows(ReportPortalException.class, () ->
        jsonPluginUploader.uploadPlugin("missing-schema.json", manifestStream));
  }

  @Test
  void uploadPluginWithInvalidJson() {
    InputStream invalidJson = new ByteArrayInputStream("{ invalid json }".getBytes());

    assertThrows(ReportPortalException.class, () ->
        jsonPluginUploader.uploadPlugin("invalid.json", invalidJson));
  }

  @Test
  void uploadPluginWithoutGroup() throws IOException {
    Map<String, Object> manifestMap = new HashMap<>();
    manifestMap.put("$schema", VALID_SCHEMA);
    manifestMap.put("id", "test-plugin");
    String manifestJson = objectMapper.writeValueAsString(manifestMap);
    InputStream manifestStream = new ByteArrayInputStream(manifestJson.getBytes());

    IntegrationType mockIntegrationType = new IntegrationType();
    when(integrationTypeRepository.findByName(any())).thenReturn(Optional.empty());
    when(integrationTypeRepository.saveAndFlush(any(IntegrationType.class)))
        .thenReturn(mockIntegrationType);
    when(jsonSchemaValidator.validate(any(String.class), any(JsonNode.class)))
        .thenReturn(Collections.emptyList());

    jsonPluginUploader.uploadPlugin("manifest.json", manifestStream);

    verify(integrationTypeRepository).findByName(any());
    verify(integrationTypeRepository).saveAndFlush(argThat(type ->
        type.getIntegrationGroup().name().equals("OTHER")));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "http://127.0.0.1:1/x",
      "http://169.254.169.254/latest/meta-data",
      "file:///etc/passwd",
      "https://evil.example/x.json",
      "https://schema.reportportal.io/../x",
      "https://schema.reportportal.io/%2e%2e/x",
      "https://schema.reportportal.io/a b"
  })
  void uploadPluginWithForeignSchemaIsRejectedWithoutValidation(String schema) throws IOException {
    Map<String, Object> manifestMap = new HashMap<>();
    manifestMap.put("$schema", schema);
    manifestMap.put("id", "test-plugin");
    InputStream manifestStream = new ByteArrayInputStream(objectMapper.writeValueAsString(manifestMap).getBytes());

    var exception = assertThrows(ReportPortalException.class, () ->
        jsonPluginUploader.uploadPlugin("manifest.json", manifestStream));

    assertEquals(ErrorType.PLUGIN_UPLOAD_ERROR, exception.getErrorType());
    verify(jsonSchemaValidator, never()).validate(any(String.class), any(JsonNode.class));
    verify(integrationTypeRepository, never()).saveAndFlush(any(IntegrationType.class));
  }
}
