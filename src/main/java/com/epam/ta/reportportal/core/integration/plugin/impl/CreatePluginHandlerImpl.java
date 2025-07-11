/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.plugin.impl;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.PluginUploadedEvent;
import com.epam.ta.reportportal.core.integration.plugin.CreatePluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.strategy.PluginUploaderFactory;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.activity.PluginActivityResource;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles the creation of plugins by uploading them to the system.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreatePluginHandlerImpl implements CreatePluginHandler {

  private final PluginUploaderFactory pluginUploaderFactory;

  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * Constructor for CreatePluginHandlerImpl.
   *
   * @param pluginUploaderFactory     Factory for creating plugin uploads
   * @param applicationEventPublisher Event publisher for reporting plugin upload events
   */
  @Autowired
  public CreatePluginHandlerImpl(
      PluginUploaderFactory pluginUploaderFactory,
      ApplicationEventPublisher applicationEventPublisher
  ) {
    this.pluginUploaderFactory = pluginUploaderFactory;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public EntryCreatedRS uploadPlugin(MultipartFile pluginFile, ReportPortalUser user) {

    String newPluginFileName = pluginFile.getOriginalFilename();
    String extension = FilenameUtils.getExtension(newPluginFileName);

    BusinessRule.expect(newPluginFileName, StringUtils::isNotBlank)
        .verify(ErrorType.BAD_REQUEST_ERROR, "File name should be not empty.");

    try (InputStream inputStream = pluginFile.getInputStream()) {
      var uploader = pluginUploaderFactory.getUploader(extension);
      IntegrationType integrationType = uploader.uploadPlugin(newPluginFileName, inputStream);
      PluginActivityResource pluginActivityResource = new PluginActivityResource();
      pluginActivityResource.setId(integrationType.getId());
      pluginActivityResource.setName(integrationType.getName());
      applicationEventPublisher.publishEvent(
          new PluginUploadedEvent(pluginActivityResource, user.getUserId(), user.getUsername()));
      return new EntryCreatedRS(integrationType.getId());
    } catch (IOException e) {
      throw new ReportPortalException(
          ErrorType.PLUGIN_UPLOAD_ERROR, "Error during file stream retrieving");
    }

  }

}
