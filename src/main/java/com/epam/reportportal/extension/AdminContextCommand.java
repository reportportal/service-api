package com.epam.reportportal.extension;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import java.util.function.Predicate;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Abstract plugin command restricted to users with the {@code ADMINISTRATOR} role, using context-based execution.
 *
 * @param <T> the return type of the command
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public abstract class AdminContextCommand<T> extends AbstractContextBasedCommand<T> {

  @Override
  public void validateRole(PluginCommandContext commandContext) {
    ReportPortalUser user = (ReportPortalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    BusinessRule.expect(UserRole.ADMINISTRATOR.equals(user.getUserRole()), Predicate.isEqual(true))
        .verify(ErrorType.ACCESS_DENIED, "Only user with role 'ADMINISTRATOR' is allowed to execute command.");
  }

}
