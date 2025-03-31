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

package com.epam.ta.reportportal.core.integration.plugin.strategy;

import static com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum.OTHER;
import static com.epam.ta.reportportal.entity.enums.PluginTypeEnum.REMOTE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.integration.plugin.PluginUploader;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.epam.ta.reportportal.util.JsonSchemaValidator;
import com.epam.ta.reportportal.ws.converter.builders.IntegrationTypeBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Uploads a plugin in JSON format.
 * <p>
 * Validates the plugin manifest against a JSON schema and saves the integration type to the
 * database.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Service
public class JsonPluginUploader implements PluginUploader {

  private final IntegrationTypeRepository integrationTypeRepository;
  private final ObjectMapper objectMapper;
  private final JsonSchemaValidator schemaValidator;

  @Autowired
  public JsonPluginUploader(
      IntegrationTypeRepository integrationTypeRepository,
      ObjectMapper objectMapper,
      JsonSchemaValidator schemaValidator
  ) {
    this.integrationTypeRepository = integrationTypeRepository;
    this.objectMapper = objectMapper;
    this.schemaValidator = schemaValidator;
  }

  @Override
  public IntegrationType uploadPlugin(String fileName, InputStream inputStream) throws IOException {
    try {

      byte[] bytes = inputStream.readAllBytes();

      var manifest = objectMapper.readValue(
          new ByteArrayInputStream(bytes),
          new TypeReference<Map<String, Object>>() {
          }
      );

      var schemaLocation = ofNullable(manifest.get("$schema"))
          .orElseThrow(
              () -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
                  Suppliers.formattedSupplier(
                      "Schema location is not specified in manifest file '{}'",
                      fileName
                  ))
          ).toString();

      var validationMessages = schemaValidator.validate(
          schemaLocation,
          new ByteArrayInputStream(bytes)
      );

      if (!validationMessages.isEmpty()) {
        throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, Suppliers.formattedSupplier(
            "Manifest file '{}' validation error: {}",
            fileName,
            validationMessages
        ));
      }

      var details = new IntegrationTypeDetails();
      details.setDetails(manifest);

      var name = ofNullable(manifest.get("id"))
          .orElseThrow(
              () -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
                  Suppliers.formattedSupplier(
                      "Plugin id is not specified in manifest file '{}'",
                      fileName
                  ))
          ).toString();

      var group = ofNullable(manifest.get("group"))
          .map(Object::toString)
          .flatMap(IntegrationGroupEnum::findByName)
          .orElse(OTHER);

      var integrationType = new IntegrationTypeBuilder()
          .setName(name)
          .setIntegrationGroup(group)
          .setDetails(details)
          .setEnabled(true)
          .setPluginType(REMOTE)
          .get();

      return integrationTypeRepository.save(integrationType);
    } catch (ConstraintViolationException e) {
      throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, Suppliers.formattedSupplier(
          e.getCause().getMessage()
      ));
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, Suppliers.formattedSupplier(
          "Manifest file '{}' read error: {}",
          fileName,
          e.getMessage()
      ));
    }
  }
}