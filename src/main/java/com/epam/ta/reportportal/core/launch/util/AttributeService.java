/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.util;

import com.epam.ta.reportportal.entity.enums.RetentionPolicyEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.reporting.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRQ;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * @author Ivan Kustau
 */
@Service
public class AttributeService {
  public void processAttributes(Launch launch, StartLaunchRQ request) {
    Optional<ItemAttributesRQ> retentionPolicyAttribute =
        request.getAttributes().stream().filter(attr -> "retention_policy".equals(attr.getKey()))
            .findFirst();

    retentionPolicyAttribute.ifPresentOrElse(attr -> {
      String value = attr.getValue();
      launch.setRetentionPolicy(RetentionPolicyEnum.valueOf(value.toUpperCase()));
    }, () -> launch.setRetentionPolicy(RetentionPolicyEnum.REGULAR));
  }
}
