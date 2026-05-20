/*
 * Copyright (C) 2026 EPAM Systems
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

package com.epam.reportportal.extension.command;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import java.util.function.Predicate;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AdminExtensionCommand<T> extends AbstractExtensionCommand<T> {

  @Override
  public void validateRole(PluginCommandContext commandContext) {
    ReportPortalUser user = (ReportPortalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    BusinessRule.expect(UserRole.ADMINISTRATOR.equals(user.getUserRole()), Predicate.isEqual(true))
        .verify(ErrorType.ACCESS_DENIED, "Only user with role 'ADMINISTRATOR' is allowed to execute command.");
  }

}
