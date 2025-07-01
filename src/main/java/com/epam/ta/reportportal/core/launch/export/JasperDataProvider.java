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
package com.epam.ta.reportportal.core.launch.export;

import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Initial {@link net.sf.jasperreports.engine.JRDataSource} provider class for RP Jasper Reports
 *
 * @author Andrei_Ramanchuk
 */
@RequiredArgsConstructor
@Service("jasperDataProvider")
public class JasperDataProvider {

  private final TestItemRepository testItemRepository;

  public Map<Long, TestItemPojo> getTestItemsOfLaunch(Launch launch, boolean includeAttachments) {
    /* Get launch referred test items with SORT! */
    return testItemRepository.selectTestItemsProjection(launch.getId())
        .stream()
        .map(item -> TestItemPojo.build(item, includeAttachments))
        .collect(Collectors.toMap(TestItemPojo::getId, it -> it, (oldValue, newValue) -> oldValue,
            LinkedHashMap::new));
  }
}