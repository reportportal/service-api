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

package com.epam.reportportal.base.core.integration.impl;

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.INTEGRATION_NOT_FOUND;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.core.integration.ExecuteIntegrationHandler;
import com.epam.reportportal.base.core.plugin.PluginBox;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationTypeDetails;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.PluginTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.util.SecurityContextUtils;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.builtin.BuiltinCommandResolver;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import com.epam.reportportal.extension.command.ExtensionCommand;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Default implementation that dispatches plugin command execution to the appropriate plugin integration.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Service
@SuppressWarnings("unchecked")
public class ExecuteIntegrationHandlerImpl implements ExecuteIntegrationHandler {

  private static final String ASYNC_MODE = "async";

  //Required field for user authorization in plugin
  private static final String PROJECT_ID = "projectId";
  private static final String PROJECT_NAME = "projectName";
  private static final String PUBLIC_COMMAND_PREFIX = "public_";
  private static final String ACCESS_TYPE_KEY = "accessType";
  private static final String ACCESS_TYPE_PUBLIC = "public";

  private final IntegrationRepository integrationRepository;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final BuiltinCommandResolver builtinCommandResolver;

  private final PluginBox pluginBox;

  private final ProjectRepository projectRepository;
  private final ProjectExtractor projectExtractor;
  private final OrganizationUserRepository organizationUserRepository;

  public ExecuteIntegrationHandlerImpl(IntegrationRepository integrationRepository,
      IntegrationTypeRepository integrationTypeRepository, BuiltinCommandResolver builtinCommandResolver,
      PluginBox pluginBox, ProjectRepository projectRepository, ProjectExtractor projectExtractor,
      OrganizationUserRepository organizationUserRepository) {
    this.integrationRepository = integrationRepository;
    this.integrationTypeRepository = integrationTypeRepository;
    this.builtinCommandResolver = builtinCommandResolver;
    this.pluginBox = pluginBox;
    this.projectRepository = projectRepository;
    this.projectExtractor = projectExtractor;
    this.organizationUserRepository = organizationUserRepository;
  }

  @Override
  public Object executeCommand(MembershipDetails membershipDetails, String pluginName,
      String command, Map<String, Object> executionParams) {
    ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            formattedSupplier("Plugin for '{}' isn't installed", pluginName).get()
        ));
    executionParams.put(PROJECT_ID, membershipDetails.getProjectId());
    executionParams.put(PROJECT_NAME, membershipDetails.getProjectKey());
    var pluginCommand = ofNullable(pluginInstance.getCommonCommand(command))
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            formattedSupplier("Command '{}' is not found in plugin {}.", command, pluginName).get()
        ));
    return pluginCommand.executeCommand(executionParams);
  }

  @Override
  public Object executePublicCommand(String pluginName, String command,
      Map<String, Object> executionParams) {
    BusinessRule.expect(command, c -> c.startsWith(PUBLIC_COMMAND_PREFIX))
        .verify(ACCESS_DENIED, formattedSupplier("Command '{}' is not public.", command).get());
    // Anonymous callers may only reach plugins that declare the public access type, the same
    // criterion the public plugin listing uses.
    boolean publicPlugin = integrationTypeRepository.findByName(pluginName)
        .map(IntegrationType::getDetails)
        .map(IntegrationTypeDetails::getDetails)
        .map(details -> details.get(ACCESS_TYPE_KEY))
        .map(ACCESS_TYPE_PUBLIC::equals)
        .orElse(false);
    if (!publicPlugin) {
      throw new ReportPortalException(ACCESS_DENIED,
          formattedSupplier("Plugin '{}' is not public.", pluginName).get());
    }
    ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(pluginName,
            ReportPortalExtensionPoint.class)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            formattedSupplier("Plugin for '{}' isn't installed", pluginName).get()
        ));
    return ofNullable(pluginInstance.getCommonCommand(command))
        .map(it -> it.executeCommand(executionParams))
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            formattedSupplier("Public command '{}' is not found in plugin {}.", command,
                pluginName).get()
        ));
  }

  @Override
  public Object executeCommand(MembershipDetails membershipDetails, Long integrationId,
      String command,
      Map<String, Object> executionParams) {
    Integration integration = integrationRepository.findByIdAndProjectId(integrationId,
            membershipDetails.getProjectId())
        .orElseGet(() -> integrationRepository.findGlobalById(integrationId)
            .orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId)));

    ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(
            integration.getType().getName(), ReportPortalExtensionPoint.class)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            formattedSupplier("Plugin for '{}' isn't installed",
                integration.getType().getName()).get()
        ));

    executionParams.put(PROJECT_ID, membershipDetails.getProjectId());

    var foundCommand = pluginInstance.getIntegrationCommand(command);
    if (foundCommand != null) {
      if (isAsyncMode(executionParams)) {
        supplyAsync(() -> foundCommand.executeCommand(integration, executionParams));
        return new OperationCompletionRS(
            formattedSupplier("Command '{}' accepted for processing in plugin",
                command,
                integration.getType().getName()
            ).get());
      }
      return foundCommand.executeCommand(integration, executionParams);
    } else {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          formattedSupplier("Command '{}' is not found in plugin {}.", command,
              integration.getType().getName()).get());
    }
  }

  @Override
  public Object executeCommand(String pluginName, String command, PluginCommandRQ pluginCommandRq) {
    ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            formattedSupplier("Plugin for '{}' isn't installed", pluginName).get()
        ));

    var pluginCommand = ofNullable(pluginInstance.getCommonCommand(command))
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            formattedSupplier("Command '{}' is not found in plugin {}.", command, pluginName).get()
        ));
    enrichArgumentsFromContext(pluginCommandRq);
    return pluginCommand.executeCommand(pluginCommandRq);
  }


  @Override
  public Object executeExtensionCommand(String pluginName, String command, PluginCommandRQ pluginCommandRq) {
    Long requestedProjectId = resolveRequestedProjectId(pluginCommandRq);
    Long requestedOrgId = ofNullable(pluginCommandRq.getContext()).map(PluginCommandContext::getOrgId).orElse(null);
    enrichArgumentsFromContext(pluginCommandRq);

    if (isBuiltinExtension(pluginName)) {
      var builtinCommand = builtinCommandResolver.resolve(pluginName, command);
      Integration integration = resolveIntegration(pluginName, pluginCommandRq.getContext());
      authorizeCommand(builtinCommand, integration, requestedProjectId, requestedOrgId);
      return builtinCommand.executeCommand(integration, pluginCommandRq);
    }

    ReportPortalExtensionPoint pluginInstance = pluginBox.getInstance(pluginName, ReportPortalExtensionPoint.class)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            formattedSupplier("Plugin for '{}' isn't installed", pluginName).get()
        ));

    if (isCommonCommand(command, pluginInstance)) {
      var commonCommand = pluginInstance.getCommonExtensionCommand(command);
      authorizeCommand(commonCommand, null, requestedProjectId, requestedOrgId);
      return commonCommand.executeCommand(pluginCommandRq);
    }

    if (isIntegrationCommand(command, pluginInstance)) {
      var integrationCommand = pluginInstance.getIntegrationExtensionCommand(command);
      Integration integration = resolveIntegration(pluginName, pluginCommandRq.getContext());
      authorizeCommand(integrationCommand, integration, requestedProjectId, requestedOrgId);
      return integrationCommand.executeCommand(integration, pluginCommandRq);
    }
    throw new ReportPortalException(BAD_REQUEST_ERROR,
        formattedSupplier("Command '{}' is not found in plugin {}.", command, pluginName).get()
    );
  }

  private static boolean isIntegrationCommand(String command, ReportPortalExtensionPoint pluginInstance) {
    return pluginInstance.getIntegrationExtensionCommand(command) != null;
  }

  private boolean isBuiltinExtension(String name) {
    return integrationTypeRepository.existsByPluginTypeAndName(PluginTypeEnum.BUILT_IN, name);
  }

  private static boolean isCommonCommand(String command, ReportPortalExtensionPoint pluginInstance) {
    return pluginInstance.getCommonExtensionCommand(command) != null;
  }

  private void enrichArgumentsFromContext(PluginCommandRQ pluginCommandRq) {
    var context = pluginCommandRq.getContext();
    if (context == null) {
      return;
    }
    var arguments = pluginCommandRq.getArguments();
    if (arguments == null) {
      arguments = new HashMap<>();
      pluginCommandRq.setArguments(arguments);
    }
    if (context.getProjectId() != null) {
      arguments.put(PROJECT_ID, context.getProjectId());
    }
  }

  /**
   * Returns the project the caller asks the command to act on: the context {@code project_id}, or, when absent, the
   * legacy {@code projectId} argument that plugins read for authorization.
   */
  private Long resolveRequestedProjectId(PluginCommandRQ pluginCommandRq) {
    var context = pluginCommandRq.getContext();
    if (context != null && context.getProjectId() != null) {
      return context.getProjectId();
    }
    var argument = ofNullable(pluginCommandRq.getArguments()).map(args -> args.get(PROJECT_ID)).orElse(null);
    if (argument == null) {
      return null;
    }
    try {
      return Long.valueOf(String.valueOf(argument));
    } catch (NumberFormatException _) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, "Argument 'projectId' must be a number");
    }
  }

  /**
   * Enforces the caller's access to the scope the command acts on, independently of the plugin's own checks. The scope
   * is derived from the integration (the project or organization it belongs to); the requested project/organization
   * must agree with it. Global integrations and common commands are scoped by the requested project/organization, if
   * any. The required role is the one declared by the command ({@link AbstractExtensionCommand}), otherwise
   * {@link ProjectRole#EDITOR} on a project and {@link OrganizationRole#MEMBER} on an organization. Organization
   * managers pass project checks within their organization; administrators bypass role checks.
   */
  private void authorizeCommand(ExtensionCommand<?> command, Integration integration, Long requestedProjectId,
      Long requestedOrgId) {
    Long projectId = requestedProjectId;
    Long orgId = requestedOrgId;
    if (integration != null && integration.getProject() != null) {
      Long integrationProjectId = integration.getProject().getId();
      BusinessRule.expect(projectId, id -> id == null || id.equals(integrationProjectId))
          .verify(ACCESS_DENIED, "Integration does not belong to the requested project");
      projectId = integrationProjectId;
    } else if (integration != null && integration.getOrganizationId() != null) {
      Long integrationOrgId = integration.getOrganizationId();
      BusinessRule.expect(orgId, id -> id == null || id.equals(integrationOrgId))
          .verify(ACCESS_DENIED, "Integration does not belong to the requested organization");
      orgId = integrationOrgId;
    }

    ReportPortalUser user = SecurityContextUtils.getPrincipal();
    BusinessRule.expect(user, Objects::nonNull).verify(ACCESS_DENIED);
    boolean isAdmin = user.getUserRole() == UserRole.ADMINISTRATOR;

    if (projectId != null) {
      Project project = projectRepository.findById(projectId)
          .orElseThrow(() -> new ReportPortalException(ACCESS_DENIED));
      Long projectOrgId = project.getOrganizationId();
      BusinessRule.expect(orgId, id -> id == null || id.equals(projectOrgId))
          .verify(ACCESS_DENIED, "Project does not belong to the requested organization");
      if (!isAdmin) {
        validateProjectRole(user, project, requiredProjectRole(command));
      }
    } else if (orgId != null && !isAdmin) {
      OrganizationRole requiredRole = requiredOrgRole(command);
      OrganizationRole orgRole = organizationUserRepository.findByUserIdAndOrganization_Id(user.getUserId(), orgId)
          .map(OrganizationUser::getOrganizationRole)
          .orElseThrow(() -> new ReportPortalException(ACCESS_DENIED));
      BusinessRule.expect(orgRole, role -> role.sameOrHigherThan(requiredRole)).verify(ACCESS_DENIED);
    }
  }

  private void validateProjectRole(ReportPortalUser user, Project project, ProjectRole requiredRole) {
    MembershipDetails membership = projectExtractor.findMembershipDetails(user, project.getKey())
        .orElseThrow(() -> new ReportPortalException(ACCESS_DENIED));
    if (membership.getOrgRole() != null && membership.getOrgRole().sameOrHigherThan(OrganizationRole.MANAGER)) {
      return;
    }
    BusinessRule.expect(membership.getProjectRole(), role -> role != null && role.sameOrHigherThan(requiredRole))
        .verify(ACCESS_DENIED);
  }

  private static ProjectRole requiredProjectRole(ExtensionCommand<?> command) {
    if (command instanceof AbstractExtensionCommand<?> roleAware && roleAware.getMinProjectRole() != null) {
      return roleAware.getMinProjectRole();
    }
    return ProjectRole.EDITOR;
  }

  private static OrganizationRole requiredOrgRole(ExtensionCommand<?> command) {
    if (command instanceof AbstractExtensionCommand<?> roleAware && roleAware.getMinOrgRole() != null) {
      return roleAware.getMinOrgRole();
    }
    return OrganizationRole.MEMBER;
  }

  private Integration resolveIntegration(String pluginName, PluginCommandContext context) {
    if (context != null && context.getIntegrationId() != null) {
      Long integrationId = context.getIntegrationId();
      return integrationRepository.findById(integrationId)
          .filter(integration -> pluginName.equals(integration.getType().getName()))
          .orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId));
    }

    Long typeId = integrationTypeRepository.findByName(pluginName)
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            formattedSupplier("Integration type '{}' not found.", pluginName).get()))
        .getId();

    if (context != null && context.getProjectId() != null) {
      return integrationRepository.findFirstEnabledByProjectIdAndTypeIdIn(context.getProjectId(), List.of(typeId))
          .orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, pluginName));
    } else if (context != null && context.getOrgId() != null) {
      return integrationRepository.findFirstEnabledByOrganizationIdAndTypeIdIn(context.getOrgId(), List.of(typeId))
          .orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, pluginName));
    }
    return integrationRepository.findFirstEnabledGlobalByTypeIdIn(List.of(typeId))
        .orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, pluginName));
  }

  @Async
  @Deprecated
  //need for security context sharing into plugin
  //it doesn't work as expected
  public <U> void supplyAsync(Supplier<U> supplier) {
    supplier.get();
  }

  private boolean isAsyncMode(Map<String, Object> executionParams) {
    return ofNullable((Boolean) executionParams.get(ASYNC_MODE)).orElse(false);
  }
}
