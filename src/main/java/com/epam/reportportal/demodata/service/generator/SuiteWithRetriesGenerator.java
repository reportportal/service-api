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

package com.epam.reportportal.demodata.service.generator;

import com.epam.reportportal.demodata.model.DemoItemMetadata;
import com.epam.reportportal.demodata.model.RootMetaData;
import com.epam.reportportal.demodata.service.DemoDataTestItemService;
import com.epam.reportportal.demodata.service.DemoLogsService;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuiteWithRetriesGenerator extends DefaultSuiteGenerator {

  private static final int RETRIES_COUNT = 3;

  @Autowired
  public SuiteWithRetriesGenerator(DemoDataTestItemService demoDataTestItemService,
      DemoLogsService demoLogsService) {
    super(demoDataTestItemService, demoLogsService);
  }

  @Override
  protected void createStep(DemoItemMetadata stepMetaData, RootMetaData rootMetaData) {
    super.createStep(stepMetaData, rootMetaData);
    if (stepMetaData.getStatus() != StatusEnum.PASSED) {
      generateRetries(stepMetaData, rootMetaData);
    }
  }

  private void generateRetries(final DemoItemMetadata metadata, RootMetaData rootMetaData) {
    IntStream.range(0, RETRIES_COUNT).forEach(i -> {
      final DemoItemMetadata retryMetaData = getMetadata(metadata.getName(),
          metadata.getType(),
          metadata.getStatus(),
          metadata.getParentId()
      ).withIssue(metadata.getIssue()).withRetry(true);
      super.createStep(retryMetaData, rootMetaData);
    });
  }
}
