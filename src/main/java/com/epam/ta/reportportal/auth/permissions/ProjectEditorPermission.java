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

package com.epam.ta.reportportal.auth.permissions;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Check whether user is organization manager or assigned to project as editor.
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
@Component("projectEditorPermission")
@LookupPermission({"projectEditor"})
class ProjectEditorPermission implements Permission {

  private final ProjectUserRepository projectUserRepository;

  ProjectEditorPermission(ProjectUserRepository projectUserRepository) {
    this.projectUserRepository = projectUserRepository;
  }

  @Override
  public boolean isAllowed(Authentication authentication, Object id) {
    ReportPortalUser rpUser = (ReportPortalUser) authentication.getPrincipal();
    BusinessRule.expect(rpUser, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

    Long projectId = Long.parseLong(String.valueOf(id));
    return projectUserRepository.findProjectUserByUserIdAndProjectId(rpUser.getUserId(), projectId)
        .filter(projectUser -> projectUser.getProjectRole().equals(ProjectRole.EDITOR))
        .isPresent();
  }
}
