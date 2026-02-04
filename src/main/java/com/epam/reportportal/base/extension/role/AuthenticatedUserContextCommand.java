package com.epam.reportportal.base.extension.role;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.base.extension.AbstractContextBasedCommand;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import java.util.Objects;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AuthenticatedUserContextCommand extends AbstractContextBasedCommand<Object> {

  protected void validateRole(PluginCommandContext commandContext) {
    var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    BusinessRule.expect(principal, Objects::nonNull)
        .verify(ErrorType.ACCESS_DENIED, "Only authenticated user is allowed to execute command.");
  }

}
