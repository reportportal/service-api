package com.epam.ta.reportportal.auth.permissions;

import static com.epam.ta.reportportal.entity.organization.OrganizationRole.MANAGER;
import static java.util.Objects.isNull;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.GroupRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.entity.group.Group;
import com.epam.ta.reportportal.entity.user.OrganizationUser;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("groupEditorPermission")
@LookupPermission({"groupEditor"})
public class GroupEditorPermission implements Permission {

  private final GroupRepository groupRepository;
  private final OrganizationUserRepository organizationUserRepository;

  @Autowired
  public GroupEditorPermission(
      GroupRepository groupRepository,
      OrganizationUserRepository organizationUserRepository
  ) {
    this.groupRepository = groupRepository;
    this.organizationUserRepository = organizationUserRepository;
  }

  @Override
  public boolean isAllowed(Authentication authentication, Object groupId) {
    if (!authentication.isAuthenticated()) {
      return false;
    }

    var rpUser = (ReportPortalUser) authentication.getPrincipal();
    BusinessRule.expect(rpUser, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

    var orgId = groupRepository.findById((Long) groupId)
        .filter(group -> !isNull(group.getOrganizationId()))
        .map(Group::getOrganizationId)
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.NOT_FOUND, "Organization group with id " + groupId
        ));

    return organizationUserRepository
        .findByUserIdAndOrganization_Id(rpUser.getUserId(), orgId)
        .map(OrganizationUser::getOrganizationRole)
        .filter(MANAGER::sameOrHigherThan)
        .isPresent();
  }
}
