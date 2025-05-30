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
import static com.epam.reportportal.rules.exception.ErrorType.RESOURCE_ALREADY_EXISTS;

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
 * Service for managing operations related to {@link Project} entities. Provides methods to retrieve and manipulate
 * project data within the ReportPortal system. This service handles basic CRUD operations and project-specific business
 * logic.
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
@Service
@Validated
public class ProjectService {

  private final ProjectRepository projectRepository;

  /**
   * Constructs a new ProjectService with the specified ProjectRepository.
   *
   * @param projectRepository the repository used for project data access, must not be {@code null}
   */
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

  /**
   * Updates the name of a project identified by its ID.
   *
   * @param projectId the ID of the project to update, must not be {@code null}
   * @param name      the new name for the project, must match pattern ^[A-Za-z0-9.'_\- ]+$ with length between 3 and 60
   *                  characters
   */
  public void updateProjectName(Long orgId, Long projectId,
      @Valid @NotNull @Pattern(regexp = "^[A-Za-z0-9.'_\\- ]+$") @Size(min = 3, max = 60) String name) {
    validateForExistingName(orgId, projectId, name);
    projectRepository.updateProjectName(name, projectId);
  }

  /**
   * Updates the slug of a project identified by its ID.
   *
   * @param projectId the ID of the project to update
   * @param slug      the new slug for the project must match a pattern ^[a-z0-9]+(?:-[a-z0-9]+)*$ with length between 3
   *                  and 60 characters
   */
  public void updateProjectSlug(Long orgId, Long projectId,
      @Valid @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") @Size(min = 3, max = 60) String slug) {
    projectRepository.updateProjectSlug(slug, projectId);
  }

  /**
   * Retrieves a project by its ID and organization ID.
   *
   * @param projectId the ID of the project to retrieve
   * @param orgId     the ID of the organization that owns the project
   * @return the found {@link Project}
   * @throws ReportPortalException if the project is not found
   */
  public Project findByProjectIdAndOrgId(Long projectId, Long orgId) {
    return projectRepository.findByIdAndOrganizationId(projectId, orgId)
        .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectId));
  }

  /**
   * Checks if a project exists within an organization.
   *
   * @param projectId the ID of the project to check
   * @param orgId     the ID of the organization to check
   * @return {@code true} if the project exists in the organization, {@code false} otherwise
   */
  public boolean existsByProjectIdAndOrgId(Long projectId, Long orgId) {
    return projectRepository.existsByIdAndOrganizationId(projectId, orgId);
  }


  /**
   * Validates that a project name doesn't already exist within an organization (except for the current project).
   *
   * @param orgId     the ID of the organization
   * @param projectId the ID of the current project (to exclude from validation)
   * @param name      the project name to validate
   * @throws ReportPortalException if another project with the same name already exists in the organization
   */
  public void validateForExistingName(Long orgId, Long projectId, String name) {
    projectRepository.findByNameAndOrganizationId(name, orgId)
        .filter(prj -> !prj.getId().equals(projectId))
        .ifPresent(v -> {
          throw new ReportPortalException(RESOURCE_ALREADY_EXISTS, "project name");
        });
  }
}
