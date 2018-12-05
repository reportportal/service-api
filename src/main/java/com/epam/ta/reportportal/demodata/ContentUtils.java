/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.demodata;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.*;
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

	static Set<ItemAttributeResource> getAttributesInRange(int limit) {
		List<Pair<String, String>> content = readAttributes("demo/content/tags.txt");
		int fromIndex = random.nextInt(content.size() - limit);
		return content.subList(fromIndex, fromIndex + limit).stream().map(it -> {
			if (it.getValue().isEmpty()) {
				return new ItemAttributeResource(it.getKey(), null);
			} else {
				return new ItemAttributeResource(it.getKey(), it.getValue());
			}
		}).collect(Collectors.toSet());

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

	static Issue getProductBug() {
		Issue issue = new Issue();
		issue.setIssueType(PRODUCT_BUG.getLocator());
		issue.setComment(bugDescription("demo/content/comments/product.txt"));
		return issue;
	}

	static Issue getAutomationBug() {
		Issue issue = new Issue();
		issue.setIssueType(AUTOMATION_BUG.getLocator());
		issue.setComment(bugDescription("demo/content/comments/automation.txt"));
		return issue;
	}

	static Issue getSystemIssue() {
		Issue issue = new Issue();
		issue.setIssueType(SYSTEM_ISSUE.getLocator());
		issue.setComment(bugDescription("demo/content/comments/system.txt"));
		return issue;
	}

	static Issue getInvestigate() {
		Issue issue = new Issue();
		issue.setIssueType(TO_INVESTIGATE.getLocator());
		issue.setComment(bugDescription("demo/content/comments/investigate.txt"));
		return issue;
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

	private static List<Pair<String, String>> readAttributes(String resource) {
		List<Pair<String, String>> content;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(resource).getInputStream(), UTF_8))) {
			content = reader.lines().map(it -> {
				if (it.contains(":")) {
					return Pair.of(it.split(":")[0], it.split(":")[1]);
				} else {
					return Pair.of(it, "");
				}
			}).collect(Collectors.toList());
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
