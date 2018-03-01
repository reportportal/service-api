/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.core.project;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.core.project.impl.UpdateProjectHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.AnalyzeMode;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserType;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.database.personal.PersonalProjectService;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCaseDTO;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Dzmitry_Kavalets
 */
@SpringFixture("unitTestsProjectTriggers")
public class UpdateProjectHandlerTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Autowired
	private UpdateProjectHandler updateProjectHandler;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PersonalProjectService personalProjectService;

	@Test
	public void checkEmptyEmailOptions() {
		String userName = "user1";
		UpdateProjectRQ updateProjectRQ = new UpdateProjectRQ();
		ProjectConfiguration configuration = new ProjectConfiguration();

		ProjectEmailConfigDTO emailConfig = new ProjectEmailConfigDTO();
		final EmailSenderCaseDTO emailSenderCase = new EmailSenderCaseDTO();
		emailSenderCase.setSendCase("ALWAYS");
		emailSenderCase.setRecipients(Collections.singletonList("demo@demo.com"));
		emailConfig.setEmailCases(singletonList(emailSenderCase));

		configuration.setEmailConfig(emailConfig);
		updateProjectRQ.setConfiguration(configuration);
		String project1 = "project1";
		updateProjectHandler.updateProject(project1, updateProjectRQ, userName);
		Project one = projectRepository.findOne(project1);
		assertNotNull(one.getConfiguration().getEmailConfig());
	}

	@Test
	public void checkConfigurationFields() {
		String userName = "user1";
		UpdateProjectRQ updateProjectRQ = new UpdateProjectRQ();
		ProjectConfiguration configuration = new ProjectConfiguration();
		configuration.setEntry("INTERNAL");
		configuration.setKeepLogs("2 weeks");
		configuration.setInterruptJobTime("1 hour");
		configuration.setKeepScreenshots("1 week");
		configuration.setProjectSpecific("DEFAULT");
		configuration.setIsAutoAnalyzerEnabled(true);
		configuration.setAnalyzerMode(AnalyzeMode.ALL_LAUNCHES.getValue());
		configuration.setStatisticCalculationStrategy("TEST_BASED");
		updateProjectRQ.setConfiguration(configuration);

		String project1 = "project1";
		updateProjectHandler.updateProject(project1, updateProjectRQ, userName);
		Project one = projectRepository.findOne(project1);

		Project.Configuration dbConfig = one.getConfiguration();
		assertEquals(configuration.getEntry(), dbConfig.getEntryType().name());
		assertEquals(configuration.getKeepLogs(), dbConfig.getKeepLogs());
		assertEquals(configuration.getInterruptJobTime(), dbConfig.getInterruptJobTime());
		assertEquals(configuration.getKeepScreenshots(), dbConfig.getKeepScreenshots());
		assertEquals(configuration.getProjectSpecific(), dbConfig.getProjectSpecific().name());
		assertEquals(configuration.getIsAutoAnalyzerEnabled(), dbConfig.getIsAutoAnalyzerEnabled());
		assertEquals(configuration.getStatisticCalculationStrategy(), dbConfig.getStatisticsCalculationStrategy().name());
	}

	@Test
	public void checkUnassignFromPersonal() {
		User user = new User();
		user.setEmail("checkUnassignFromPersonal@gmail.com");
		user.setLogin("checkUnassignFromPersonal");
		user.setType(UserType.INTERNAL);
		Project project = personalProjectService.generatePersonalProject(user);
		userRepository.save(user);
		projectRepository.save(project);

		UnassignUsersRQ rq = new UnassignUsersRQ();
		rq.setUsernames(singletonList(user.getLogin()));

		exception.expect(ReportPortalException.class);
		exception.expectMessage(containsString("Unable to unassign user from his personal project"));
		updateProjectHandler.unassignUsers(project.getName(), AuthConstants.ADMINISTRATOR.getName(), rq);

	}
}
