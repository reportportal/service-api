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

package com.epam.reportportal.auth.endpoint;


import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

/**
 * Shows list of supported user roles.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Component
public class MetadataContributor implements InfoContributor {

  @Override
  public void contribute(Info.Builder builder) {
    builder
        .withDetail("metadata", ImmutableMap.builder()
            .put("project_roles", Arrays.stream(ProjectRole.values())
                .map(Enum::name)
                .toList())
            .build());
  }

}
