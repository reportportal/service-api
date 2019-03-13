/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.util.email;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.statistics.StatisticsField;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class EmailServiceTest {

	private EmailService emailService = mock(EmailService.class);

	@Test
	void sendCreateUserConfirmEmailTest() {

		doNothing().when(emailService).send(any(MimeMessagePreparator.class));

		emailService.sendCreateUserConfirmationEmail("subj", new String[] { "email@email.com" }, "epam.com");
	}

	@Test
	void sendLaunchFinishedNotificationTest() {

		doNothing().when(emailService).send(any(MimeMessagePreparator.class));

		emailService.sendLaunchFinishNotification(new String[] { "email@email.com" }, "epam.com", "project name", getLaunch());
	}

	@Test
	void sendRestorePasswordEmailTest() {

		doNothing().when(emailService).send(any(MimeMessagePreparator.class));

		emailService.sendRestorePasswordEmail("restore", new String[] { "email@email.com" }, "url", "login");
	}

	@Test
	void sendIndexFinishedEmailTest() {

		doNothing().when(emailService).send(any(MimeMessagePreparator.class));

		emailService.sendIndexFinishedEmail("restore", "email@email.com", 10L);
	}

	@Test
	void sendCreateUserConfirmationEmailTest() {

		CreateUserRQFull createUserRQFull = new CreateUserRQFull();
		createUserRQFull.setLogin("login");
		createUserRQFull.setPassword("password");
		createUserRQFull.setEmail("email@email.com");

		doNothing().when(emailService).send(any(MimeMessagePreparator.class));

		emailService.sendCreateUserConfirmationEmail(createUserRQFull, "url");
	}

	private Launch getLaunch() {
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setHasRetries(false);
		launch.setStatus(StatusEnum.PASSED);
		launch.setProjectId(1L);
		launch.setStartTime(LocalDateTime.now());
		launch.setEndTime(LocalDateTime.now().plusMinutes(5L));
		launch.setName("Launch name");
		launch.setMode(LaunchModeEnum.DEFAULT);
		launch.setNumber(1L);
		launch.setDescription("description");
		launch.setAttributes(Sets.newHashSet(new ItemAttribute("key", "value", false)));
		StatisticsField statisticsField = new StatisticsField("statistics$executions$total");
		Statistics statistics = new Statistics(statisticsField, 1, 1L);
		launch.setStatistics(Sets.newHashSet(statistics));
		return launch;
	}

}