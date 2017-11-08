package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserPreferenceRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.email.ProjectEmailConfig;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCaseDTO;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationEventPublisher;

import java.util.stream.IntStream;

import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MAX_LOGIN_LENGTH;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MIN_LOGIN_LENGTH;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpdateProjectHandlerTest extends BaseTest {

	@Rule
	public ExpectedException expected = none();
	private final String project = "project";
	private final String user = "user";
	private UpdateProjectHandler updateProjectHandler;

	@Before
	public void before() {
		final ProjectRepository projectRepository = mock(ProjectRepository.class);
		final Project project = new Project();
		project.setName(this.project);
		final Project.Configuration configuration = new Project.Configuration();
		configuration.setEmailConfig(new ProjectEmailConfig());
		project.setConfiguration(configuration);
		when(projectRepository.findOne(this.project)).thenReturn(project);
		updateProjectHandler = new UpdateProjectHandler(projectRepository, mock(UserRepository.class), mock(UserPreferenceRepository.class),
				mock(ApplicationEventPublisher.class)
		);
	}

	@Test
	public void updateProjectEmailConfigProjectNotExists() throws Exception {
		expected.expect(ReportPortalException.class);
		expected.expectMessage("Project 'notExists' not found. Did you use correct project name?");
		updateProjectHandler.updateProjectEmailConfig("notExists", user, new ProjectEmailConfigDTO());
	}

	@Test
	public void invalidFromField() {
		final ProjectEmailConfigDTO projectEmailConfigDTO = new ProjectEmailConfigDTO();
		final String from = "fake@from@.com";
		projectEmailConfigDTO.setFrom(from);
		projectEmailConfigDTO.setEmailEnabled(true);
		expected.expect(ReportPortalException.class);
		expected.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Provided FROM value '" + from + "' is invalid'");
		updateProjectHandler.updateProjectEmailConfig(project, user, projectEmailConfigDTO);
	}

	@Test
	public void emptyCases() {
		final ProjectEmailConfigDTO projectEmailConfigDTO = new ProjectEmailConfigDTO();
		projectEmailConfigDTO.setEmailCases(emptyList());
		projectEmailConfigDTO.setFrom("user1@fake.com");
		projectEmailConfigDTO.setEmailEnabled(true);
		expected.expect(ReportPortalException.class);
		expected.expectMessage("Error in handled Request. Please, check specified parameters: 'At least one rule should be present.'");
		updateProjectHandler.updateProjectEmailConfig(project, user, projectEmailConfigDTO);
	}

	@Test
	public void invalidSendCase() {
		final String invalid = "invalid";
		final ProjectEmailConfigDTO projectEmailConfigDTO = new ProjectEmailConfigDTO();
		final EmailSenderCaseDTO emailSenderCase = new EmailSenderCaseDTO();
		emailSenderCase.setSendCase(invalid);
		projectEmailConfigDTO.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfigDTO.setFrom("user1@fake.com");
		projectEmailConfigDTO.setEmailEnabled(true);
		expected.expect(ReportPortalException.class);
		expected.expectMessage("Error in handled Request. Please, check specified parameters: '" + invalid + "'");
		updateProjectHandler.updateProjectEmailConfig(project, user, projectEmailConfigDTO);
	}

	@Test
	public void nullRecipients() {
		final String always = "ALWAYS";
		final ProjectEmailConfigDTO projectEmailConfigDTO = new ProjectEmailConfigDTO();
		final EmailSenderCaseDTO emailSenderCase = new EmailSenderCaseDTO();
		emailSenderCase.setSendCase(always);
		projectEmailConfigDTO.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfigDTO.setFrom("user1@fake.com");
		projectEmailConfigDTO.setEmailEnabled(true);
		expected.expect(ReportPortalException.class);
		expected.expectMessage("Error in handled Request. Please, check specified parameters: 'Recipients list should not be null'");
		updateProjectHandler.updateProjectEmailConfig(project, user, projectEmailConfigDTO);
	}

	@Test
	public void emptyRecipients() {
		final String always = "ALWAYS";
		final ProjectEmailConfigDTO projectEmailConfigDTO = new ProjectEmailConfigDTO();
		final EmailSenderCaseDTO emailSenderCase = new EmailSenderCaseDTO();
		emailSenderCase.setSendCase(always);
		emailSenderCase.setRecipients(emptyList());
		projectEmailConfigDTO.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfigDTO.setFrom("user1@fake.com");
		projectEmailConfigDTO.setEmailEnabled(true);

		expected.expect(ReportPortalException.class);
		expected.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Empty recipients list for email case '" + emailSenderCase
						+ "' '");
		updateProjectHandler.updateProjectEmailConfig(project, user, projectEmailConfigDTO);
	}

	@Test
	public void recipientsInvalidEmail() {
		final String always = "ALWAYS";
		final ProjectEmailConfigDTO projectEmailConfig = new ProjectEmailConfigDTO();
		final EmailSenderCaseDTO emailSenderCase = new EmailSenderCaseDTO();
		emailSenderCase.setSendCase(always);
		final String email = "email@email@email.com";
		emailSenderCase.setRecipients(singletonList(email));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		projectEmailConfig.setEmailEnabled(true);
		expected.expect(ReportPortalException.class);
		expected.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Provided recipient email '" + email + "' is invalid'");
		updateProjectHandler.updateProjectEmailConfig(project, user, projectEmailConfig);
	}

	@Test
	public void recipientsNullValue() {
		final String always = "ALWAYS";
		final ProjectEmailConfigDTO projectEmailConfigDTO = new ProjectEmailConfigDTO();
		final EmailSenderCaseDTO emailSenderCase = new EmailSenderCaseDTO();
		emailSenderCase.setSendCase(always);
		final String email = "email@email@email.com";
		emailSenderCase.setRecipients(asList(null, email));
		projectEmailConfigDTO.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfigDTO.setFrom("user1@fake.com");
		projectEmailConfigDTO.setEmailEnabled(true);
		expected.expect(ReportPortalException.class);
		expected.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Provided recipient email '" + null + "' is invalid'");
		updateProjectHandler.updateProjectEmailConfig(project, user, projectEmailConfigDTO);
	}

	@Test
	public void emptyLogin() {
		final String always = "ALWAYS";
		final ProjectEmailConfigDTO projectEmailConfig = new ProjectEmailConfigDTO();
		final EmailSenderCaseDTO emailSenderCase = new EmailSenderCaseDTO();
		emailSenderCase.setSendCase(always);
		emailSenderCase.setRecipients(singletonList(""));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		projectEmailConfig.setEmailEnabled(true);
		expected.expect(ReportPortalException.class);
		expected.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Acceptable login length  [" + MIN_LOGIN_LENGTH + ".."
						+ MAX_LOGIN_LENGTH + "]'");
		updateProjectHandler.updateProjectEmailConfig(project, user, projectEmailConfig);
	}

	@Test
	public void projectWithoutSuchUser() {
		final String always = "ALWAYS";
		final ProjectEmailConfigDTO projectEmailConfig = new ProjectEmailConfigDTO();
		final EmailSenderCaseDTO emailSenderCase = new EmailSenderCaseDTO();
		emailSenderCase.setSendCase(always);
		emailSenderCase.setRecipients(singletonList("user"));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		projectEmailConfig.setEmailEnabled(true);
		expected.expect(ReportPortalException.class);
		expected.expectMessage("User '" + user + "' not found. User not found in project " + project);
		updateProjectHandler.updateProjectEmailConfig(project, user, projectEmailConfig);
	}

	@Test
	public void nullLaunchesNames() {
		final String always = "ALWAYS";
		final ProjectEmailConfigDTO projectEmailConfig = new ProjectEmailConfigDTO();
		final EmailSenderCaseDTO emailSenderCase = new EmailSenderCaseDTO();
		emailSenderCase.setSendCase(always);
		emailSenderCase.setRecipients(singletonList("user@fake.com"));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		projectEmailConfig.setEmailEnabled(true);
		emailSenderCase.setLaunchNames(singletonList(null));
		expected.expect(ReportPortalException.class);
		expected.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Launch name values cannot be empty. Please specify it or not include in request.'");
		updateProjectHandler.updateProjectEmailConfig(project, user, projectEmailConfig);
	}

	@Test
	public void launchName257Length() {
		final String always = "ALWAYS";
		final ProjectEmailConfigDTO projectEmailConfig = new ProjectEmailConfigDTO();
		final EmailSenderCaseDTO emailSenderCase = new EmailSenderCaseDTO();
		emailSenderCase.setSendCase(always);
		emailSenderCase.setRecipients(singletonList("user@fake.com"));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		projectEmailConfig.setEmailEnabled(true);
		final String launchName = IntStream.range(0, 258).mapToObj(it -> "i").collect(joining());
		emailSenderCase.setLaunchNames(singletonList(launchName));
		expected.expect(ReportPortalException.class);
		expected.expectMessage("Error in handled Request. Please, check specified parameters: 'One of provided launch names '" + launchName
				+ "' is too long. Acceptable name length is [1..256]'");
		updateProjectHandler.updateProjectEmailConfig(project, user, projectEmailConfig);
	}

	@Test
	public void emptyTag() {
		final String always = "ALWAYS";
		final ProjectEmailConfigDTO projectEmailConfig = new ProjectEmailConfigDTO();
		final EmailSenderCaseDTO emailSenderCase = new EmailSenderCaseDTO();
		emailSenderCase.setSendCase(always);
		emailSenderCase.setRecipients(singletonList("user@fake.com"));
		emailSenderCase.setTags(asList("", "tag"));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		projectEmailConfig.setEmailEnabled(true);
		expected.expect(ReportPortalException.class);
		expected.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Tags values cannot be empty. Please specify it or not include in request.'");
		updateProjectHandler.updateProjectEmailConfig(project, user, projectEmailConfig);
	}

}