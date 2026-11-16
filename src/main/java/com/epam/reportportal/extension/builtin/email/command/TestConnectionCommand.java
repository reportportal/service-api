package com.epam.reportportal.extension.builtin.email.command;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.core.integration.util.EmailServerIntegrationService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestConnectionCommand extends AbstractExtensionCommand<Boolean> {

  private final EmailServerIntegrationService emailServerIntegrationService;

  public TestConnectionCommand(ProjectRepository projectRepository, OrganizationRepository organizationRepository,
      OrganizationUserRepository organizationUserRepository, ProjectUserRepository projectUserRepository,
      EmailServerIntegrationService emailServerIntegrationService) {
    super(projectRepository, organizationUserRepository, organizationRepository, projectUserRepository);

    this.minProjectRole = ProjectRole.VIEWER;
    this.minOrgRole = OrganizationRole.MEMBER;
    this.minUserRole = UserRole.USER;
    this.emailServerIntegrationService = emailServerIntegrationService;
  }

  @Override
  public String getName() {
    return "testConnection";
  }

  @Override
  public Boolean executeCommand(Integration integration, PluginCommandRQ pluginCommandRq) {
    return emailServerIntegrationService.checkConnection(integration);
  }
}
