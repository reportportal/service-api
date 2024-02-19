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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.model.DeleteBulkRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import java.util.List;

/**
 * Delete {@link com.epam.ta.reportportal.entity.project.Project} request handler
 *
 * @author Hanna_Sukhadolava
 */
public interface DeleteProjectHandler {

  /**
   * Delete specified project.
   *
   * @param projectId Project id
   * @param user      {@link ReportPortalUser}
   * @return Result of operation
   * @throws ReportPortalException if project not found
   */
  OperationCompletionRS deleteProject(Long projectId, ReportPortalUser user);

  /**
   * Delete specified project.
   *
   * @param ids  projects ids
   * @param user {@link ReportPortalUser}
   * @return Bulk result of operation
   * @throws ReportPortalException if project not found
   */
  DeleteBulkRS bulkDeleteProjects(List<Long> ids, ReportPortalUser user);

  /**
   * Delete project index.
   *
   * @param projectName Project name
   * @param username    User name
   * @return {@link OperationCompletionRS} info about operation completion
   */
  OperationCompletionRS deleteProjectIndex(String projectName, String username);

}