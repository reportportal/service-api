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

package com.epam.ta.reportportal.core.project;

import static com.epam.reportportal.rules.exception.ErrorType.PROJECT_NOT_FOUND;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Service for managing operations related to {@link Project} entities. Provides methods to retrieve and manipulate project data.
 */
@Service
@Validated
public class ProjectService {

  private final ProjectRepository projectRepository;

  @Autowired
  public ProjectService(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  /**
   * Retrieves a {@link Project} by its unique identifier.
   *
   * @param projectId the ID of the project to retrieve, must not be {@code null}
   * @return the found {@link Project}
   * @throws ReportPortalException if the project with the given ID is not found
   */
  public Project findProjectById(Long projectId) {
    return projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectId));
  }

  public void updateProjectName(Long projectId,
      @Valid @NotNull @Pattern(regexp = "^[A-Za-z0-9.'_\\- ]+$") @Size(min = 3, max = 60) String name) {
    projectRepository.updateProjectName(name, projectId);
  }

  public void updateProjectSlug(Long projectId,
      @Valid @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") @Size(min = 3, max = 60) String slug) {
    projectRepository.updateProjectSlug(slug, projectId);
  }

  public Project findByProjectIdAndOrgId(Long projectId, Long orgId) {
    return projectRepository.findByIdAndOrganizationId(projectId, orgId)
        .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectId));
  }

  public boolean existsByProjectIdAndOrgId(Long projectId, Long orgId) {
    return projectRepository.existsByIdAndOrganizationId(projectId, orgId);
  }
}
