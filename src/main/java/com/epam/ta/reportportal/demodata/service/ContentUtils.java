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

package com.epam.ta.reportportal.demodata.service;

import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.function.Supplier;
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
public final class ContentUtils {

	private static final int MAX_ERROR_LOGS_COUNT = 2;

	private static final int ERRORS_COUNT = 9;

	private static final Range<Integer> PROBABILITY_RANGE = Range.openClosed(0, 100);

	private static SplittableRandom random = new SplittableRandom();

	private static final Map<TestItemIssueGroup, Supplier<Issue>> ISSUE_MAPPING = Map.of(PRODUCT_BUG,
			() -> getIssue(PRODUCT_BUG.getLocator(), bugDescription("demo/content/comments/product.txt")),
			AUTOMATION_BUG,
			() -> getIssue(AUTOMATION_BUG.getLocator(), bugDescription("demo/content/comments/automation.txt")),
			SYSTEM_ISSUE,
			() -> getIssue(SYSTEM_ISSUE.getLocator(), bugDescription("demo/content/comments/system.txt")),
			TO_INVESTIGATE,
			() -> getIssue(TO_INVESTIGATE.getLocator(), bugDescription("demo/content/comments/investigate.txt"))
	);

	private ContentUtils() {
		//static only
	}

	public static String getNameFromType(TestItemTypeEnum type) {
		return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, type.name());
	}

	public static Set<ItemAttributesRQ> getAttributesInRange(int limit) {
		List<Pair<String, String>> content = readAttributes("demo/content/attributes.txt");
		int fromIndex = random.nextInt(content.size() - limit);
		return content.subList(fromIndex, fromIndex + limit).stream().map(it -> {
			if (it.getKey().isEmpty()) {
				return new ItemAttributesRQ(null, it.getValue());
			} else {
				return new ItemAttributesRQ(it.getKey(), it.getValue());
			}
		}).collect(Collectors.toSet());

	}

	public static String getSuiteDescription() {
		List<String> content = readToList("demo/content/suite-description.txt");
		return content.get(random.nextInt(content.size()));
	}

	public static String getStepDescription() {
		List<String> content = readToList("demo/content/step-description.txt");
		return content.get(random.nextInt(content.size()));
	}

	public static String getTestDescription() {
		List<String> content = readToList("demo/content/test-description.txt");
		return content.get(random.nextInt(content.size()));
	}

	public static String getLaunchDescription() {
		return readToString("demo/content/description.txt");
	}

	public static List<String> getErrorLogs() {
		return IntStream.range(0, MAX_ERROR_LOGS_COUNT).mapToObj(i -> {
			int errorNumber = random.nextInt(1, ERRORS_COUNT);
			return readToString("demo/errors/" + errorNumber + ".txt");
		}).collect(Collectors.toList());
	}

	public static String getLogMessage() {
		List<String> logs = readToList("demo/content/demo_logs.txt");
		return logs.get(random.nextInt(logs.size()));
	}

	public static boolean getWithProbability(int probability) {
		Preconditions.checkArgument(PROBABILITY_RANGE.contains(probability), "%s is not in range [%s]", probability, PROBABILITY_RANGE);
		return Range.closedOpen(PROBABILITY_RANGE.lowerEndpoint(), probability).contains(random.nextInt(PROBABILITY_RANGE.upperEndpoint()));
	}

	public static Issue getIssue(TestItemIssueGroup group) {
		return ISSUE_MAPPING.get(group).get();
	}

	private static Issue getIssue(String locator, String comment) {
		Issue issue = new Issue();
		issue.setIssueType(locator);
		issue.setComment(comment);
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
					return Pair.of("", it);
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
