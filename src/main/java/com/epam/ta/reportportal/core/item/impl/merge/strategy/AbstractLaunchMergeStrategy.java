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

package com.epam.ta.reportportal.core.item.impl.merge.strategy;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.item.identity.IdentityUtil;
import com.epam.ta.reportportal.core.item.identity.TestItemUniqueIdGenerator;
import com.epam.ta.reportportal.core.item.merge.LaunchMergeStrategy;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.ta.reportportal.ws.converter.converters.ItemAttributeConverter.FROM_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.FINISH_TIME_EARLIER_THAN_START_TIME;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractLaunchMergeStrategy implements LaunchMergeStrategy {

	private final TestItemRepository testItemRepository;

	private final TestItemUniqueIdGenerator identifierGenerator;

	protected final LaunchRepository launchRepository;

	public AbstractLaunchMergeStrategy(TestItemRepository testItemRepository, TestItemUniqueIdGenerator identifierGenerator,
			LaunchRepository launchRepository) {
		this.testItemRepository = testItemRepository;
		this.identifierGenerator = identifierGenerator;
		this.launchRepository = launchRepository;
	}

	protected Launch createNewLaunch(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, MergeLaunchesRQ rq,
			List<Launch> launchesList) {
		Launch newLaunch = createResultedLaunch(projectDetails.getProjectId(), user.getUserId(), rq, launchesList);
		boolean isNameChanged = !newLaunch.getName().equals(launchesList.get(0).getName());
		updateChildrenOfLaunches(newLaunch, rq.getLaunches(), rq.isExtendSuitesDescription(), isNameChanged);

		return newLaunch;
	}

	/**
	 * Create launch that will be the result of merge
	 *
	 * @param projectId       {@link Project#id}
	 * @param userId          {@link ReportPortalUser#userId}
	 * @param mergeLaunchesRQ {@link MergeLaunchesRQ}
	 * @param launches        {@link List} of the {@link Launch}
	 * @return launch
	 */
	private Launch createResultedLaunch(Long projectId, Long userId, MergeLaunchesRQ mergeLaunchesRQ, List<Launch> launches) {
		Date startTime = ofNullable(mergeLaunchesRQ.getStartTime()).orElse(EntityUtils.TO_DATE.apply(launches.stream()
				.min(Comparator.comparing(Launch::getStartTime))
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Invalid launches"))
				.getStartTime()));
		Date endTime = ofNullable(mergeLaunchesRQ.getEndTime()).orElse(EntityUtils.TO_DATE.apply(launches.stream()
				.max(Comparator.comparing(Launch::getEndTime))
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Invalid launches"))
				.getEndTime()));
		expect(endTime, time -> !time.before(startTime)).verify(FINISH_TIME_EARLIER_THAN_START_TIME,
				TO_LOCAL_DATE_TIME.apply(endTime),
				startTime,
				projectId
		);

		StartLaunchRQ startRQ = new StartLaunchRQ();
		startRQ.setMode(ofNullable(mergeLaunchesRQ.getMode()).orElse(Mode.DEFAULT));
		startRQ.setDescription(ofNullable(mergeLaunchesRQ.getDescription()).orElse(launches.stream()
				.map(Launch::getDescription)
				.collect(joining("\n\n"))));
		startRQ.setName(ofNullable(mergeLaunchesRQ.getName()).orElse(
				"Merged: " + launches.stream().map(Launch::getName).distinct().collect(joining(", "))));
		startRQ.setStartTime(startTime);
		Launch launch = new LaunchBuilder().addStartRQ(startRQ)
				.addProject(projectId)
				.addStatus(IN_PROGRESS.name())
				.addUserId(userId)
				.addEndTime(endTime)
				.get();
		launch.setHasRetries(launches.stream().anyMatch(Launch::isHasRetries));

		launchRepository.save(launch);
		launchRepository.refresh(launch);
		mergeAttributes(mergeLaunchesRQ.getAttributes(), launches, launch);
		return launch;
	}

	/**
	 * Merges launches attributes. Collect all system attributes from existed launches
	 * and all unique not system attributes from request(if preset, or from exited launches if not) to resulted launch.
	 *
	 * @param attributesFromRq {@link Set} of attributes from request
	 * @param launchesToMerge  {@link List} of {@link Launch} to be merged
	 * @param resultedLaunch   {@link Launch} - result of merge
	 */

	private void mergeAttributes(Set<ItemAttributeResource> attributesFromRq, List<Launch> launchesToMerge, Launch resultedLaunch) {
		Set<ItemAttribute> mergedAttributes = Sets.newHashSet();

		if (attributesFromRq == null) {
			mergedAttributes.addAll(launchesToMerge.stream()
					.map(Launch::getAttributes)
					.flatMap(Collection::stream)
					.peek(it -> it.setLaunch(resultedLaunch))
					.collect(Collectors.toSet()));
		} else {
			mergedAttributes.addAll(launchesToMerge.stream()
					.map(Launch::getAttributes)
					.flatMap(Collection::stream)
					.filter(ItemAttribute::isSystem)
					.peek(it -> it.setLaunch(resultedLaunch))
					.collect(Collectors.toSet()));
			mergedAttributes.addAll(attributesFromRq.stream()
					.map(FROM_RESOURCE)
					.peek(attr -> attr.setLaunch(resultedLaunch))
					.collect(Collectors.toSet()));
		}
		resultedLaunch.setAttributes(mergedAttributes);
	}

	/**
	 * Update test-items of specified launches with new LaunchID
	 *
	 * @param newLaunch         {@link Launch}
	 * @param launches          {@link Set} of the {@link Launch}
	 * @param extendDescription additional description for suite indicator
	 * @param isNameChanged     launch name change indicator
	 */
	private void updateChildrenOfLaunches(Launch newLaunch, Set<Long> launches, boolean extendDescription, boolean isNameChanged) {
		List<TestItem> testItems = launches.stream().flatMap(id -> {
			Launch launch = launchRepository.findById(id).orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, id));
			return testItemRepository.findTestItemsByLaunchId(launch.getId()).stream().peek(testItem -> {
				testItem.setLaunchId(newLaunch.getId());
				if (isNameChanged && identifierGenerator.validate(testItem.getUniqueId())) {
					testItem.setUniqueId(identifierGenerator.generate(testItem, IdentityUtil.getParentIds(testItem), newLaunch));
				}
				if (testItem.getType().sameLevel(TestItemTypeEnum.SUITE)) {
					// Add launch reference description for top level items
					Supplier<String> newDescription = Suppliers.formattedSupplier(
							((null != testItem.getDescription()) ? testItem.getDescription() : "") + (extendDescription ?
									"\r\n@launch '{} #{}'" :
									""), launch.getName(), launch.getNumber());
					testItem.setDescription(newDescription.get());
				}
			});
		}).collect(toList());
		testItemRepository.saveAll(testItems);
	}
}
