package com.epam.ta.reportportal.core.bts.handler.impl;

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.converter.builders.BugTrackingSystemBuilder;
import com.epam.ta.reportportal.ws.converter.converters.IntegrationConverter;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.CreateExternalSystemRQ;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.INTEGRATION_NOT_FOUND;

/**
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Service
public class IntegrationsHandler {

	@Autowired
	private BasicTextEncryptor simpleEncryptor;

	@Autowired
	private IntegrationRepository integrationRepository;

	@Autowired
	private IntegrationTypeRepository integrationTypeRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private PluginBox pluginBox;

	public IntegrationResource getIntegrationByID(String projectName, Long id) {

		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		Integration integration = integrationRepository.findByIdAndProjectId(id, project.getId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, id));
		return IntegrationConverter.TO_INTEGRATION_RESOURCE.apply(integration);
	}

	public synchronized OperationCompletionRS deleteIntegration(String projectName, Long systemId, ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);

		Integration integration = integrationRepository.findByIdAndProjectId(systemId, projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, systemId));

		integrationRepository.delete(integration);

		//		eventPublisher.publishEvent(new ExternalSystemDeletedEvent(exist, username));
		return new OperationCompletionRS("ExternalSystem with ID = '" + systemId + "' is successfully deleted.");
	}

	public synchronized OperationCompletionRS deleteProjectIntegrations(String projectName, ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);
		List<Integration> btsSystems = integrationRepository.findAllByProjectId(projectDetails.getProjectId());
		if (!CollectionUtils.isEmpty(btsSystems)) {
			integrationRepository.deleteAll(btsSystems);
		}
		//eventPublisher.publishEvent(new ProjectExternalSystemsDeletedEvent(exist, projectName, username));
		return new OperationCompletionRS("All ExternalSystems for project '" + projectName + "' successfully removed");
	}

	public EntryCreatedRS createExternalSystem(CreateExternalSystemRQ createRQ, String projectName, ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);

		//		Integration externalSystemStrategy = strategyProvider.getStrategy(createRQ.getExternalSystemType());
		//		expect(externalSystemStrategy, notNull()).verify(INTEGRATION_NOT_FOUND, createRQ.getExternalSystemType());

		Optional<IntegrationType> type = integrationTypeRepository.findByName(createRQ.getExternalSystemType());
		expect(type, Optional::isPresent).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, projectName);

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, "with id = " + projectDetails.getProjectId()));

		Integration bugTrackingSystem = new BugTrackingSystemBuilder().addUrl(createRQ.getUrl())
				.addIntegrationType(type.get())
				.addBugTrackingProject(createRQ.getProject())
				.addProject(project)
				.addUsername(createRQ.getUsername())
				//TODO encryption here and decryption on the plugin side
				.addPassword(createRQ.getPassword())
				//				.addPassword(simpleEncryptor.encrypt(createRQ.getPassword()))
				.addAuthType(createRQ.getExternalSystemAuth())
				.get();

		//		checkUnique(bugTrackingSystem, projectDetails.getProjectId());

		Optional<BtsExtension> extenstion = pluginBox.getInstance(createRQ.getExternalSystemType(), BtsExtension.class);
		expect(extenstion, Optional::isPresent).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, projectName);

		expect(extenstion.get().connectionTest(bugTrackingSystem), BooleanUtils::isTrue).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				projectName
		);

		integrationRepository.save(bugTrackingSystem);
		//eventPublisher.publishEvent(new IntegrationCreatedEvent(createOne, username));
		return new EntryCreatedRS(bugTrackingSystem.getId());
	}

	//	//TODO probably could be handled by database
	//	private void checkUnique(BugTrackingSystem bugTrackingSystem, Long projectId) {
	//		integrationRepository.findByUrlAndBtsProjectAndProjectId(
	//				bugTrackingSystem.getUrl(), bugTrackingSystem.getBtsProject(), projectId)
	//				.ifPresent(it -> new ReportPortalException(ErrorType.INTEGRATION_ALREADY_EXISTS,
	//						bugTrackingSystem.getUrl() + " & " + bugTrackingSystem.getBtsProject()
	//				));
	//	}

}
