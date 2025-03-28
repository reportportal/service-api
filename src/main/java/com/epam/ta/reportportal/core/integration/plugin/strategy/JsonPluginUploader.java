package com.epam.ta.reportportal.core.integration.plugin.strategy;

import com.epam.ta.reportportal.core.integration.plugin.PluginUploader;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JsonPluginUploader implements PluginUploader {

  private final IntegrationTypeRepository integrationTypeRepository;

  @Autowired
  public JsonPluginUploader(IntegrationTypeRepository integrationTypeRepository) {
    this.integrationTypeRepository = integrationTypeRepository;
  }

  @Override
  public IntegrationType uploadPlugin(String fileName, InputStream inputStream) throws IOException {
    // JsonSchemaValidator.validate(inputStream);
     IntegrationType integrationType = new IntegrationType();
//
    //    integrationType.setDetails(...);
    return integrationTypeRepository.save(integrationType);
  }
}