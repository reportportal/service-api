package com.epam.reportportal.extension;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import java.util.function.Predicate;
import org.springframework.security.core.context.SecurityContextHolder;

/**
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
