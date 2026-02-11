package com.epam.reportportal.extension;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class ProjectMemberCommand<T> extends AbstractRoleBasedCommand<T> {

  public static final String PROJECT_ID_PARAM = "projectId";


  protected final ProjectRepository projectRepository;
  protected final OrganizationRepositoryCustom organizationRepository;


  protected ProjectMemberCommand(ProjectRepository projectRepository,
      OrganizationRepositoryCustom organizationRepository) {
    this.projectRepository = projectRepository;
    this.organizationRepository = organizationRepository;
  }

  @Override
  public void validateRole(Map<String, Object> params) {
    Long projectId = retrieveLong(params, PROJECT_ID_PARAM);
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));

    ReportPortalUser user = (ReportPortalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    BusinessRule.expect(user, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

    validatePermissions(user, project);
  }

  protected void validatePermissions(ReportPortalUser user, Project project) {
    if (user.getUserRole() == UserRole.ADMINISTRATOR) {
      return;
    }
    Organization organization = organizationRepository.findById(project.getOrganizationId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND));

    OrganizationRole orgRole = ofNullable(user.getOrganizationDetails())
        .flatMap(detailsMapping -> ofNullable(detailsMapping.get(organization.getName())))
        .map(ReportPortalUser.OrganizationDetails::getOrgRole)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));

    if (orgRole.sameOrHigherThan(OrganizationRole.MANAGER)) {
      return;
    }

    user.getOrganizationDetails().entrySet().stream()
        .filter(entry -> entry.getKey().equals(organization.getName()))
        .map(Entry::getValue)
        .flatMap(orgDetails -> orgDetails.getProjectDetails().entrySet().stream())
        .map(Entry::getValue)
        .filter(details -> details.getProjectId().equals(project.getId()))
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));
  }

  public static Long retrieveLong(Map<String, Object> params, String param) {
    return ofNullable(params.get(param)).map(String::valueOf)
        .map(ProjectMemberCommand::safeParseLong)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            Suppliers.formattedSupplier("Parameter '{}' was not provided", param).get()
        ));
  }

  public static Long safeParseLong(String param) {
    try {
      return Long.parseLong(param);
    } catch (NumberFormatException ex) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, ex.getMessage());
    }
  }
}
