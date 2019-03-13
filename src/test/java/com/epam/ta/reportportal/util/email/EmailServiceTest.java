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

import com.epam.reportportal.commons.template.TemplateEngine;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.statistics.StatisticsField;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class EmailServiceTest {

	private TemplateEngine templateEngine = mock(TemplateEngine.class);

	private EmailService emailService = new EmailService(new Properties());

	@BeforeEach
	void setUp() {
		emailService.setTemplateEngine(templateEngine);
	}

	@Test
	void prepareLaunchTest() {

		when(templateEngine.merge(any(String.class), any(Map.class))).thenReturn("EMAIL MESSAGE");

		String url = emailService.mergeFinishLaunchText("url", getLaunch());

		System.out.println(url);
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