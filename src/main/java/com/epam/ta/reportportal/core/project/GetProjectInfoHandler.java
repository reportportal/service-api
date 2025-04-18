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

package com.epam.ta.reportportal.core.project;

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.enums.InfoInterval;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.model.project.ProjectInfoResource;
import java.util.Map;
import org.springframework.data.domain.Pageable;

/**
 * Get {@link ProjectInfoResource} request handler
 *
 * @author Dzmitry_Kavalets
 */
public interface GetProjectInfoHandler {

  /**
   * Get all projects info
   *
   * @param filter   Queryable filter to apply on the projects
   * @param pageable Pagination information for the results
   * @return An {@link Page} of {@link ProjectInfoResource} containing information about all
   * projects
   */
  Page<ProjectInfoResource> getAllProjectsInfo(Queryable filter, Pageable pageable);

  /**
   * Get project info
   *
   * @param projectName Project name
   * @param interval    Interval
   * @return Project info resource
   */
  ProjectInfoResource getProjectInfo(String projectName, String interval);

  /**
   * Get widget data content for specified project by specified {@link InfoInterval} and
   * {@link com.epam.ta.reportportal.entity.project.email.ProjectInfoWidget}
   *
   * @param projectName Project name
   * @param interval    Interval
   * @param widgetCode  Project Info Widget code
   * @return Map of widget data content
   */
  Map<String, ?> getProjectInfoWidgetContent(String projectName, String interval,
      String widgetCode);
}
