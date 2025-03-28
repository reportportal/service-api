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
import com.epam.ta.reportportal.ws.converter.builders.IntegrationTypeBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JsonPluginUploader implements PluginUploader {

  private final IntegrationTypeRepository integrationTypeRepository;
  private final ObjectMapper objectMapper;

  @Autowired
  public JsonPluginUploader(
      IntegrationTypeRepository integrationTypeRepository,
      ObjectMapper objectMapper
  ) {
    this.integrationTypeRepository = integrationTypeRepository;
    this.objectMapper = objectMapper;
  }

  @Override
  public IntegrationType uploadPlugin(String fileName, InputStream inputStream) throws IOException {
    // JsonSchemaValidator.validate(inputStream);
    try {
      var manifest = objectMapper.readValue(
          inputStream,
          new TypeReference<Map<String, Object>>() {
          }
      );

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
          "Manifest file '{}' read error",
          fileName
      ));
    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, Suppliers.formattedSupplier(
          "Plugin manifest '{}' upload error",
          fileName
      ));
    }
  }
}