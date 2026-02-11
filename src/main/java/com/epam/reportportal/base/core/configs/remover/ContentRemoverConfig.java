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

package com.epam.reportportal.base.core.configs.remover;

import com.epam.reportportal.base.core.remover.project.ProjectClusterRemover;
import com.epam.reportportal.base.core.remover.project.ProjectContentRemover;
import com.epam.reportportal.base.core.remover.project.ProjectWidgetRemover;
import com.epam.reportportal.base.core.remover.user.UserContentRemover;
import com.epam.reportportal.base.core.remover.user.UserPhotoRemover;
import com.epam.reportportal.base.core.remover.user.UserWidgetRemover;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class ContentRemoverConfig {

  @Bean
  public ProjectContentRemover projectContentRemover(
      @Autowired ProjectClusterRemover projectClusterRemover,
      @Autowired ProjectWidgetRemover projectWidgetRemover) {
    return new ProjectContentRemover(List.of(projectClusterRemover, projectWidgetRemover));
  }

  @Bean
  public UserContentRemover userContentRemover(@Autowired UserWidgetRemover userWidgetRemover,
      @Autowired UserPhotoRemover userPhotoRemover) {
    return new UserContentRemover(List.of(userWidgetRemover, userPhotoRemover));
  }

}
