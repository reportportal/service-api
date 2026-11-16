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

package com.epam.reportportal.base.demodata.service.generator;

import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemTypeEnum.STEP;

import com.epam.reportportal.base.demodata.model.DemoItemMetadata;
import com.epam.reportportal.base.demodata.model.RootMetaData;
import com.epam.reportportal.base.demodata.model.Test;
import com.epam.reportportal.base.demodata.service.DemoDataTestItemService;
import com.epam.reportportal.base.demodata.service.DemoLogsService;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuiteWithNestedStepsGenerator extends DefaultSuiteGenerator {

  @Autowired
  public SuiteWithNestedStepsGenerator(DemoDataTestItemService demoDataTestItemService,
      DemoLogsService demoLogsService) {
    super(demoDataTestItemService, demoLogsService);
  }

  @Override
  protected void createStep(DemoItemMetadata stepMetaData, RootMetaData rootMetaData) {
    super.createStep(stepMetaData.withNested(true), rootMetaData);
  }

  @Override
  protected String startTest(String suiteId, RootMetaData rootMetaData, Test test,
      StatusEnum testStatus) {
    final String testId = super.startTest(suiteId, rootMetaData, test, testStatus);
    final DemoItemMetadata stepParentMetadata = getMetadata(test.getName(), STEP, testStatus,
        testId);
    return demoDataTestItemService.startTestItem(stepParentMetadata, rootMetaData);
  }

}
