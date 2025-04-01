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

package com.epam.ta.reportportal.plugin;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.integration.plugin.strategy.JsonPluginUploader;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.util.JsonSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
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
  private ObjectMapper objectMapper;

  @InjectMocks
  private JsonPluginUploader jsonPluginUploader;

  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() throws Exception {
    closeable.close();
  }

  @Test
  void uploadPlugin() throws IOException {
    Map<String, Object> manifestMap = new HashMap<>();
    manifestMap.put("$schema", "https://schema.url");
    manifestMap.put("id", "test-plugin");
    manifestMap.put("group", "BTS");
    String manifestJson = objectMapper.writeValueAsString(manifestMap);
    InputStream manifestStream = new ByteArrayInputStream(manifestJson.getBytes());

    IntegrationType mockIntegrationType = new IntegrationType();
    when(integrationTypeRepository.save(any(IntegrationType.class))).thenReturn(
        mockIntegrationType);
    when(jsonSchemaValidator.validate(any(String.class), any(JsonNode.class))).thenReturn(
        Collections.emptySet());

    IntegrationType result = jsonPluginUploader.uploadPlugin("manifest.json", manifestStream);

    verify(integrationTypeRepository).save(any(IntegrationType.class));
    verify(jsonSchemaValidator).validate(any(String.class), any(JsonNode.class));
    verify(integrationTypeRepository).save(argThat(type ->
        type.getIntegrationGroup().name().equals("BTS")));
    assertNotNull(result, "Resulting IntegrationType should not be null");
  }

  @Test
  void uploadPluginWithEmptyID() throws IOException {
    Map<String, Object> manifestMap = new HashMap<>();
    manifestMap.put("$schema", "https://schema.url");
    String manifestWithoutId = objectMapper.writeValueAsString(manifestMap);
    InputStream manifestStream = new ByteArrayInputStream(manifestWithoutId.getBytes());

    when(jsonSchemaValidator.validate(any(String.class), any(JsonNode.class)))
        .thenReturn(Collections.emptySet());

    assertThrows(ReportPortalException.class, () ->
        jsonPluginUploader.uploadPlugin("missing-id.json", manifestStream));
  }

  @Test
  void uploadPluginWithMissingSchemaLocation() throws IOException {
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
    manifestMap.put("$schema", "https://schema.url");
    manifestMap.put("id", "test-plugin");
    String manifestJson = objectMapper.writeValueAsString(manifestMap);
    InputStream manifestStream = new ByteArrayInputStream(manifestJson.getBytes());

    IntegrationType mockIntegrationType = new IntegrationType();
    when(integrationTypeRepository.save(any(IntegrationType.class)))
        .thenReturn(mockIntegrationType);
    when(jsonSchemaValidator.validate(any(String.class), any(JsonNode.class)))
        .thenReturn(Collections.emptySet());

    jsonPluginUploader.uploadPlugin("manifest.json", manifestStream);

    verify(integrationTypeRepository).save(argThat(type ->
        type.getIntegrationGroup().name().equals("OTHER")));
  }
}
