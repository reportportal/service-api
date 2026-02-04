package com.epam.reportportal.base.extension;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import java.util.Map;
import java.util.function.Predicate;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public abstract class AdminCommand<T> extends AbstractRoleBasedCommand<T> {

  @Override
  public void validateRole(Map<String, Object> params) {
    ReportPortalUser user = (ReportPortalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    BusinessRule.expect(UserRole.ADMINISTRATOR.equals(user.getUserRole()), Predicate.isEqual(true))
        .verify(ErrorType.ACCESS_DENIED, "Only user with role 'ADMINISTRATOR' is allowed to execute command.");
  }

}
