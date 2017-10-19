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

package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * By reason of demo data generation is used not so often,
 * we don't need to cache the files' content.
 *
 * @author Pavel_Bortnik
 */
final class ContentUtils {

	private static final int MAX_ERROR_LOGS_COUNT = 2;

	private static final int ERRORS_COUNT = 9;

	private static final Range<Integer> PROBABILITY_RANGE = Range.openClosed(0, 100);

	private static SplittableRandom random = new SplittableRandom();

	private ContentUtils() {
		//static only
	}

	static Set<String> getTagsInRange(int limit) {
		List<String> content = readToList("demo/content/tags.txt");
		int fromIndex = random.nextInt(content.size() - limit);
		return ImmutableSet.<String>builder().addAll(content.subList(fromIndex, fromIndex + limit)).build();
	}

	static String getSuiteDescription() {
		List<String> content = readToList("demo/content/suite-description.txt");
		return content.get(random.nextInt(content.size()));
	}

	static String getStepDescription() {
		List<String> content = readToList("demo/content/step-description.txt");
		return content.get(random.nextInt(content.size()));
	}

	static String getTestDescription() {
		List<String> content = readToList("demo/content/test-description.txt");
		return content.get(random.nextInt(content.size()));
	}

	static String getDefectDescription(String issueType) {
		List<String> content = readToList("demo/content/defects.txt");
		return content.get(random.nextInt(content.size()));
	}

	static String getLaunchDescription() {
		return readToString("demo/content/description.txt");
	}

	static List<String> getErrorLogs() {
		return IntStream.range(0, MAX_ERROR_LOGS_COUNT).mapToObj(i -> {
			int errorNumber = random.nextInt(1, ERRORS_COUNT);
			return readToString("demo/errors/" + errorNumber + ".txt");
		}).collect(Collectors.toList());
	}

	static String getLogMessage() {
		List<String> logs = readToList("demo/content/demo_logs.txt");
		return logs.get(random.nextInt(logs.size()));
	}

	static boolean getWithProbability(int probability) {
		Preconditions.checkArgument(PROBABILITY_RANGE.contains(probability), "%s is not in range [%s]", probability, PROBABILITY_RANGE);
		return Range.closedOpen(PROBABILITY_RANGE.lowerEndpoint(), probability).contains(random.nextInt(PROBABILITY_RANGE.upperEndpoint()));
	}

	static TestItemIssue getProductBug() {
		return new TestItemIssue("PB001", bugDescription("demo/content/comments/product.txt"));
	}

	static TestItemIssue getAutomationBug() {
		return new TestItemIssue("AB001", bugDescription("demo/content/comments/automation.txt"));
	}

	static TestItemIssue getSystemIssue() {
		return new TestItemIssue("SI001", bugDescription("demo/content/comments/system.txt"));
	}

	static TestItemIssue getInvestigate() {
		return new TestItemIssue("TI001", bugDescription("demo/content/comments/investigate.txt"));
	}

	private static String bugDescription(String resource) {
		String description = null;
		if (random.nextBoolean()) {
			List<String> descriptions = readToList(resource);
			description = descriptions.get(random.nextInt(descriptions.size()));
		}
		return description;
	}

	private static List<String> readToList(String resource) {
		List<String> content;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(resource).getInputStream(), UTF_8))) {
			content = reader.lines().collect(Collectors.toList());
		} catch (IOException e) {
			throw new ReportPortalException("Missing demo content.", e);
		}
		return content;
	}

	private static String readToString(String resource) {
		String content;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(resource).getInputStream(), UTF_8))) {
			content = reader.lines().collect(Collectors.joining("\n"));
		} catch (IOException e) {
			throw new ReportPortalException("Missing demo content.", e);
		}
		return content;
	}
}
