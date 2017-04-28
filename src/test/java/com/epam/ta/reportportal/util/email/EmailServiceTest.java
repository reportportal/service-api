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

package com.epam.ta.reportportal.util.email;

import com.epam.ta.reportportal.core.configs.EmailConfiguration;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Properties;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author Andrei Varabyeu
 */
public class EmailServiceTest {

	@Test
	public void mergeFinishLaunchText() {
		EmailService emailService = new EmailService(new Properties());
		emailService.setTemplateEngine(new EmailConfiguration().getTemplateEngine());

		Launch launch = new Launch();
		launch.setId(UUID.randomUUID().toString());
		launch.setEndTime(Calendar.getInstance().getTime());
		launch.setName("hello world");
		launch.setNumber(1L);


		Statistics statistics = new Statistics(new ExecutionCounter(10, 5, 4, 1), new IssueCounter());
		launch.setStatistics(statistics);

		Project.Configuration settings = new Project.Configuration();

		String text = emailService.mergeFinishLaunchText("http://google.com", launch, settings);
		Assert.assertThat(text, is(not(nullValue())));
	}

}