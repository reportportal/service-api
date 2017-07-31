/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.dao.FailReferenceResourceRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.project.email.EmailSenderCase;
import com.epam.ta.reportportal.database.entity.project.email.ProjectEmailConfig;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.util.analyzer.IIssuesAnalyzer;
import com.epam.ta.reportportal.util.analyzer.INewIssuesAnalyzer;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.controller.impl.TestItemController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.inject.Provider;
import java.util.Collections;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.SendCase.*;
import static com.epam.ta.reportportal.events.handler.LaunchFinishedEventHandler.isLaunchNameMatched;
import static com.epam.ta.reportportal.events.handler.LaunchFinishedEventHandler.isSuccessRateEnough;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class LaunchFinishedEventHandlerTest {

	private LaunchFinishedEventHandler launchFinishedEventHandler;
	private UserRepository userRepository;
	private EmailService emailService;

	@Before
	public void before() {
		userRepository = mock(UserRepository.class);
		User owner = new User();
		owner.setEmail("owner@fake.com");
		User user1 = new User();
		user1.setEmail("user1@fake.com");
		User user2 = new User();
		user2.setEmail("user2@fake.com");
		when(userRepository.findOne("owner")).thenReturn(owner);
		when(userRepository.findOne("user1")).thenReturn(user1);
		when(userRepository.findOne("user2")).thenReturn(user2);
		when(userRepository.findOne("notExists")).thenReturn(null);

		final Provider provider = mock(Provider.class);
		when(provider.get()).thenReturn(new MockHttpServletRequest(HttpMethod.PUT.name(), "https://localhost:8443"));
		emailService = mock(EmailService.class);
		launchFinishedEventHandler = new LaunchFinishedEventHandler(mock(INewIssuesAnalyzer.class), userRepository,
				mock(TestItemRepository.class), provider, mock(LaunchRepository.class), new MailServiceFactory(null, null, null) {
			@Override
			public Optional<EmailService> getDefaultEmailService() {
				return Optional.of(emailService);
			}
		}, mock(FailReferenceResourceRepository.class));
	}

	@Test
	public void arrayOfRecipients() {

		String[] recipients = launchFinishedEventHandler
				.findRecipients("owner", asList("OWNER", "user1", "user2", "user3@fake.com", "user4@fake.com", "notExists"));
		assertThat(recipients).isNotNull().hasSize(5)
				.contains("owner@fake.com", "user1@fake.com", "user2@fake.com", "user3@fake.com", "user4@fake.com");
		verify(userRepository, times(4)).findOne(anyString());
	}

	@Test
	public void alwaysCaseTest() {
		assertThat(isSuccessRateEnough(new Launch(), ALWAYS)).isTrue();
	}

	@Test
	public void failedLaunchPassed() {
		final Launch launch = new Launch();
		launch.setStatus(Status.PASSED);
		assertThat(isSuccessRateEnough(launch, FAILED)).isFalse();
	}

	@Test
	public void failedLaunchFailed() {
		final Launch launch = new Launch();
		launch.setStatus(Status.FAILED);
		assertThat(isSuccessRateEnough(launch, FAILED)).isTrue();
	}

	@Test
	public void lessThan10() {
		final Launch launch = new Launch();
		launch.setStatistics(new Statistics(new ExecutionCounter(100, 91, 7, 2),
				new IssueCounter(singletonMap("total", 3), singletonMap("total", 1), singletonMap("total", 2), singletonMap("total", 3),
						singletonMap("total", 0))));
		assertThat(isSuccessRateEnough(launch, MORE_10)).isFalse();
	}

	@Test
	public void moreThan10() {
		final Launch launch = new Launch();
		launch.setStatistics(new Statistics(new ExecutionCounter(100, 89, 8, 3),
				new IssueCounter(singletonMap("total", 3), singletonMap("total", 2), singletonMap("total", 2), singletonMap("total", 4),
						singletonMap("total", 0))));
		assertThat(isSuccessRateEnough(launch, MORE_10)).isTrue();
	}

	@Test
	public void lessThan20() {
		final Launch launch = new Launch();
		launch.setStatistics(new Statistics(new ExecutionCounter(100, 81, 17, 2),
				new IssueCounter(singletonMap("total", 16), singletonMap("total", 1), singletonMap("total", 1), singletonMap("total", 1),
						singletonMap("total", 0))));
		assertThat(isSuccessRateEnough(launch, MORE_20)).isFalse();
	}

	@Test
	public void moreThan20() {
		final Launch launch = new Launch();
		launch.setStatistics(new Statistics(new ExecutionCounter(100, 79, 8, 13),
				new IssueCounter(singletonMap("total", 1), singletonMap("total", 2), singletonMap("total", 6), singletonMap("total", 14),
						singletonMap("total", 0))));
		assertThat(isSuccessRateEnough(launch, MORE_20)).isTrue();
	}

	@Test
	public void lessThan50() {
		final Launch launch = new Launch();
		launch.setStatistics(new Statistics(new ExecutionCounter(100, 51, 47, 2),
				new IssueCounter(singletonMap("total", 40), singletonMap("total", 4), singletonMap("total", 2), singletonMap("total", 3),
						singletonMap("total", 0))));
		assertThat(isSuccessRateEnough(launch, MORE_50)).isFalse();
	}

	@Test
	public void moreThan50() {
		final Launch launch = new Launch();
		launch.setStatistics(new Statistics(new ExecutionCounter(100, 49, 50, 1),
				new IssueCounter(singletonMap("total", 48), singletonMap("total", 1), singletonMap("total", 1), singletonMap("total", 1),
						singletonMap("total", 0))));
		assertThat(isSuccessRateEnough(launch, MORE_50)).isTrue();
	}

	@Test
	public void investigateLaunchWithoutToInvestigate() {
		final Launch launch = new Launch();
		launch.setStatistics(new Statistics(new ExecutionCounter(10, 9, 1, 0),
				new IssueCounter(singletonMap("total", 0), singletonMap("total", 0), singletonMap("total", 1), singletonMap("total", 0),
						singletonMap("total", 0))));
		assertThat(isSuccessRateEnough(launch, TO_INVESTIGATE)).isFalse();
	}

	@Test
	public void investigateLaunchWithToInvestigate() {
		final Launch launch = new Launch();
		launch.setStatistics(new Statistics(new ExecutionCounter(10, 9, 1, 0),
				new IssueCounter(singletonMap("total", 0), singletonMap("total", 0), singletonMap("total", 0), singletonMap("total", 1),
						singletonMap("total", 0))));
		assertThat(isSuccessRateEnough(launch, TO_INVESTIGATE)).isTrue();
	}

	@Test
	public void caseContainsLaunchName() {
		EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setLaunchNames(Collections.singletonList("launch"));
		Launch launch = new Launch();
		launch.setName("launch");
		assertThat(isLaunchNameMatched(launch, emailSenderCase)).isTrue();
	}

	@Test
	public void caseNullLaunchName() {
		assertThat(isLaunchNameMatched(null, new EmailSenderCase())).isTrue();
	}

	@Test
	public void caseEmptyLaunchesNames() {
		final EmailSenderCase oneCase = new EmailSenderCase();
		oneCase.setLaunchNames(emptyList());
		assertThat(isLaunchNameMatched(null, oneCase)).isTrue();
	}

	@Test
	public void caseDifferentLaunchesNames() {
		final Launch launch = new Launch();
		launch.setName("launch1");
		final EmailSenderCase oneCase = new EmailSenderCase();
		oneCase.setLaunchNames(singletonList("launch2"));
		assertThat(isLaunchNameMatched(launch, oneCase)).isFalse();
	}

	@Test
	public void sendEmailRightNowTest() {
		String launchName = "launch";
		final Launch launch = new Launch();
		launch.setName(launchName);
		final Project project = new Project();
		final Project.Configuration configuration = new Project.Configuration();
		final ProjectEmailConfig emailConfig = new ProjectEmailConfig();
		final EmailSenderCase emailSenderCase = new EmailSenderCase();
		emailSenderCase.setSendCase("ALWAYS");
		emailSenderCase.setLaunchNames(singletonList(launchName));
		emailSenderCase.setRecipients(singletonList("user@fake.com"));
		emailConfig.setEmailCases(singletonList(emailSenderCase));
		configuration.setEmailConfig(emailConfig);
		project.setConfiguration(configuration);
		launchFinishedEventHandler.sendEmailRightNow(launch, project, emailService);
		verify(emailService, times(1)).sendLaunchFinishNotification(anyVararg(), anyString(), any(Launch.class), any());
	}

}