package com.epam.reportportal.extension.role;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.extension.AbstractContextBasedCommand;
import java.util.Objects;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Abstract plugin command that requires only an authenticated principal.
 *
 * @deprecated Use {@link com.epam.reportportal.extension.command.AbstractExtensionCommand} instead.
 */
@Deprecated
public abstract class AuthenticatedUserContextCommand extends AbstractContextBasedCommand<Object> {

  protected void validateRole(PluginCommandContext commandContext) {
    var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    BusinessRule.expect(principal, Objects::nonNull)
        .verify(ErrorType.ACCESS_DENIED, "Only authenticated user is allowed to execute command.");
  }

}
