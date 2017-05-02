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
import com.google.common.collect.ImmutableMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author Andrei Varabyeu
 */
public class EmailServiceTest {

	@Test
	public void mergeFinishLaunchText() throws IOException {
		EmailService emailService = new EmailService(new Properties());
		emailService.setTemplateEngine(new EmailConfiguration().getTemplateEngine());

		Launch launch = new Launch();
		launch.setId(UUID.randomUUID().toString());
		launch.setEndTime(Calendar.getInstance().getTime());
		launch.setName("hello world");
		launch.setNumber(1L);

		//@formatter:off
		Statistics statistics = new Statistics(new ExecutionCounter(10, 5, 4, 1),
				new IssueCounter(
						ImmutableMap.<String, Integer>builder()
								.put(IssueCounter.GROUP_TOTAL, 3)
								.put("PB1", 3)
								.build(),
						ImmutableMap.<String, Integer>builder()
								.put("AB1", 5)
								.put(IssueCounter.GROUP_TOTAL, 5)
								.build(),
						ImmutableMap.<String, Integer>builder()
								.put("SI1", 6)
								.put(IssueCounter.GROUP_TOTAL, 6)
								.build(),
						ImmutableMap.<String, Integer>builder()
								.put(IssueCounter.GROUP_TOTAL, 7)
								.put("TI1", 3)
								.build(),
						ImmutableMap.<String, Integer>builder()
								.put(IssueCounter.GROUP_TOTAL, 3)
								.put("NI1", 3)
								.build()));
		//@formatter:on

		launch.setStatistics(statistics);

		Project.Configuration settings = new Project.Configuration();

		String text = emailService.mergeFinishLaunchText("http://google.com", launch, settings);
		Assert.assertThat(text, is(not(nullValue())));

		Document doc = Jsoup.parse(text);

		Assert.assertThat("Incorrect 'TOTAL' count", getBugCount(doc, "TOTAL"), is(10));
		Assert.assertThat("Incorrect 'Passed' count", getStatisticsCount(doc, "Passed"), is(5));
		Assert.assertThat("Incorrect 'Failed' count", getStatisticsCount(doc, "Failed"), is(4));
		Assert.assertThat("Incorrect 'Skipped' count", getStatisticsCount(doc, "Skipped"), is(1));

		Assert.assertThat("Incorrect auth bug count", getBugCount(doc, "Automation Bugs"), is(5));
		Assert.assertThat("Incorrect prod bug count", getBugCount(doc, "Product Bugs"), is(3));
		Assert.assertThat("Incorrect system bug count", getBugCount(doc, "System Issues"), is(6));
		Assert.assertThat("Incorrect 'to investigate' count", getBugCount(doc, "To Investigate"), is(7));
		Assert.assertThat("Incorrect 'no defect' count", getBugCount(doc, "No Defects"), is(3));
	}

	private int getBugCount(Document doc, String group) {
		return Integer.parseInt(doc.select(String.format("b:contains(%s)", group)).parents().get(0).nextElementSibling().text());
	}

	private int getStatisticsCount(Document doc, String group) {
		return Integer.parseInt(doc.select(String.format("td:contains(%s)", group)).last().nextElementSibling().text());
	}

}