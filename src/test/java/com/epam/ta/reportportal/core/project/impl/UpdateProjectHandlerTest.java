package com.epam.ta.reportportal.core.project.impl;

import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MAX_LOGIN_LENGTH;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MIN_LOGIN_LENGTH;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationEventPublisher;

import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserPreferenceRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCase;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfig;
import com.epam.ta.reportportal.ws.model.project.email.UpdateProjectEmailRQ;

public class UpdateProjectHandlerTest {

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
		updateProjectHandler = new UpdateProjectHandler(projectRepository, mock(UserRepository.class),
				mock(UserPreferenceRepository.class), mock(ApplicationEventPublisher.class));
	}

	@Test
	public void updateProjectEmailConfigProjectNotExists() throws Exception {
		expected.expect(ReportPortalException.class);
		expected.expectMessage("Project 'notExists' not found. Did you use correct project name?");
		updateProjectHandler.updateProjectEmailConfig("notExists", user, new UpdateProjectEmailRQ());
	}

	@Test
	public void invalidFromField() {
		final UpdateProjectEmailRQ updateProjectEmailRQ = new UpdateProjectEmailRQ();
		final ProjectEmailConfig projectEmailConfig = new ProjectEmailConfig();
		final String from = "fake@from@.com";
		projectEmailConfig.setFrom(from);
		updateProjectEmailRQ.setConfiguration(projectEmailConfig);
		expected.expect(ReportPortalException.class);
		expected.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Provided FROM value '" + from + "' is invalid'");
		updateProjectHandler.updateProjectEmailConfig(project, user, updateProjectEmailRQ);
	}

	@Test
	public void emptyCases() {
		final UpdateProjectEmailRQ updateProjectEmailRQ = new UpdateProjectEmailRQ();
		final ProjectEmailConfig projectEmailConfig = new ProjectEmailConfig();
		projectEmailConfig.setEmailCases(emptyList());
		projectEmailConfig.setFrom("user1@fake.com");
		updateProjectEmailRQ.setConfiguration(projectEmailConfig);
		expected.expect(ReportPortalException.class);
		expected.expectMessage("Error in handled Request. Please, check specified parameters: 'At least one rule should be present.'");
		updateProjectHandler.updateProjectEmailConfig(project, user, updateProjectEmailRQ);
	}

	@Test
	public void invalidSendCase() {
		final String invalid = "invalid";
		final UpdateProjectEmailRQ updateProjectEmailRQ = new UpdateProjectEmailRQ();
		final ProjectEmailConfig projectEmailConfig = new ProjectEmailConfig();
		final EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSendCase(invalid);
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		updateProjectEmailRQ.setConfiguration(projectEmailConfig);
		expected.expect(ReportPortalException.class);
		expected.expectMessage("Error in handled Request. Please, check specified parameters: '" + invalid + "'");
		updateProjectHandler.updateProjectEmailConfig(project, user, updateProjectEmailRQ);
	}

	@Test
	public void nullRecipients() {
		final String always = "ALWAYS";
		final UpdateProjectEmailRQ updateProjectEmailRQ = new UpdateProjectEmailRQ();
		final ProjectEmailConfig projectEmailConfig = new ProjectEmailConfig();
		final EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSendCase(always);
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		updateProjectEmailRQ.setConfiguration(projectEmailConfig);
		expected.expect(ReportPortalException.class);
		expected.expectMessage("Error in handled Request. Please, check specified parameters: 'Recipients list should not be null'");
		updateProjectHandler.updateProjectEmailConfig(project, user, updateProjectEmailRQ);
	}

	@Test
	public void emptyRecipients() {
		final String always = "ALWAYS";
		final UpdateProjectEmailRQ updateProjectEmailRQ = new UpdateProjectEmailRQ();
		final ProjectEmailConfig projectEmailConfig = new ProjectEmailConfig();
		final EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSendCase(always);
		emailSenderCase.setRecipients(emptyList());
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		updateProjectEmailRQ.setConfiguration(projectEmailConfig);
		expected.expect(ReportPortalException.class);
		expected.expectMessage("Error in handled Request. Please, check specified parameters: 'Empty recipients list for email case '"
				+ emailSenderCase + "' '");
		updateProjectHandler.updateProjectEmailConfig(project, user, updateProjectEmailRQ);
	}

	@Test
	public void recipientsInvalidEmail() {
		final String always = "ALWAYS";
		final UpdateProjectEmailRQ updateProjectEmailRQ = new UpdateProjectEmailRQ();
		final ProjectEmailConfig projectEmailConfig = new ProjectEmailConfig();
		final EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSendCase(always);
		final String email = "email@email@email.com";
		emailSenderCase.setRecipients(singletonList(email));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		updateProjectEmailRQ.setConfiguration(projectEmailConfig);
		expected.expect(ReportPortalException.class);
		expected.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Provided recipient email '" + email + "' is invalid'");
		updateProjectHandler.updateProjectEmailConfig(project, user, updateProjectEmailRQ);
	}

	@Test
	public void recipientsNullValue() {
		final String always = "ALWAYS";
		final UpdateProjectEmailRQ updateProjectEmailRQ = new UpdateProjectEmailRQ();
		final ProjectEmailConfig projectEmailConfig = new ProjectEmailConfig();
		final EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSendCase(always);
		final String email = "email@email@email.com";
		emailSenderCase.setRecipients(asList(null, email));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		updateProjectEmailRQ.setConfiguration(projectEmailConfig);
		expected.expect(ReportPortalException.class);
		expected.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Provided recipient email '" + null + "' is invalid'");
		updateProjectHandler.updateProjectEmailConfig(project, user, updateProjectEmailRQ);
	}

	@Test
	public void emptyLogin() {
		final String always = "ALWAYS";
		final UpdateProjectEmailRQ updateProjectEmailRQ = new UpdateProjectEmailRQ();
		final ProjectEmailConfig projectEmailConfig = new ProjectEmailConfig();
		final EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSendCase(always);
		emailSenderCase.setRecipients(singletonList(""));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		updateProjectEmailRQ.setConfiguration(projectEmailConfig);
		expected.expect(ReportPortalException.class);
		expected.expectMessage("Error in handled Request. Please, check specified parameters: 'Acceptable login length  ["
				+ MIN_LOGIN_LENGTH + ".." + MAX_LOGIN_LENGTH + "]'");
		updateProjectHandler.updateProjectEmailConfig(project, user, updateProjectEmailRQ);
	}

	@Test
	public void projectWithoutSuchUser() {
		final String always = "ALWAYS";
		final UpdateProjectEmailRQ updateProjectEmailRQ = new UpdateProjectEmailRQ();
		final ProjectEmailConfig projectEmailConfig = new ProjectEmailConfig();
		final EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSendCase(always);
		emailSenderCase.setRecipients(singletonList("user"));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		updateProjectEmailRQ.setConfiguration(projectEmailConfig);
		expected.expect(ReportPortalException.class);
		expected.expectMessage("User '" + user + "' not found. User not found in project " + project);
		updateProjectHandler.updateProjectEmailConfig(project, user, updateProjectEmailRQ);
	}

	@Test
	public void nullLaunchesNames() {
		final String always = "ALWAYS";
		final UpdateProjectEmailRQ updateProjectEmailRQ = new UpdateProjectEmailRQ();
		final ProjectEmailConfig projectEmailConfig = new ProjectEmailConfig();
		final EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSendCase(always);
		emailSenderCase.setRecipients(singletonList("user@fake.com"));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		emailSenderCase.setLaunchNames(singletonList(null));
		updateProjectEmailRQ.setConfiguration(projectEmailConfig);
		expected.expect(ReportPortalException.class);
		expected.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Launch name values cannot be empty. Please specify it or not include in request.'");
		updateProjectHandler.updateProjectEmailConfig(project, user, updateProjectEmailRQ);
	}

	@Test
	public void launchName257Length() {
		final String always = "ALWAYS";
		final UpdateProjectEmailRQ updateProjectEmailRQ = new UpdateProjectEmailRQ();
		final ProjectEmailConfig projectEmailConfig = new ProjectEmailConfig();
		final EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSendCase(always);
		emailSenderCase.setRecipients(singletonList("user@fake.com"));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		final String launchName = IntStream.range(0, 258).mapToObj(it -> "i").collect(joining());
		emailSenderCase.setLaunchNames(singletonList(launchName));
		updateProjectEmailRQ.setConfiguration(projectEmailConfig);
		expected.expect(ReportPortalException.class);
		expected.expectMessage("Error in handled Request. Please, check specified parameters: 'One of provided launch names '" + launchName
				+ "' is too long. Acceptable name length is [1..256]'");
		updateProjectHandler.updateProjectEmailConfig(project, user, updateProjectEmailRQ);
	}

	@Test
	public void emptyTag() {
		final String always = "ALWAYS";
		final UpdateProjectEmailRQ updateProjectEmailRQ = new UpdateProjectEmailRQ();
		final ProjectEmailConfig projectEmailConfig = new ProjectEmailConfig();
		final EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSendCase(always);
		emailSenderCase.setRecipients(singletonList("user@fake.com"));
		emailSenderCase.setTags(asList("", "tag"));
		projectEmailConfig.setEmailCases(singletonList(emailSenderCase));
		projectEmailConfig.setFrom("user1@fake.com");
		updateProjectEmailRQ.setConfiguration(projectEmailConfig);
		expected.expect(ReportPortalException.class);
		expected.expectMessage(
				"Error in handled Request. Please, check specified parameters: 'Tags values cannot be empty. Please specify it or not include in request.'");
		updateProjectHandler.updateProjectEmailConfig(project, user, updateProjectEmailRQ);
	}

}