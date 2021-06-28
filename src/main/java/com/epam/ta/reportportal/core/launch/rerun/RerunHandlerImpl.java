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

package com.epam.ta.reportportal.core.launch.rerun;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.events.item.ItemRetryEvent;
import com.epam.ta.reportportal.core.item.identity.IdentityUtil;
import com.epam.ta.reportportal.core.item.identity.TestCaseHashGenerator;
import com.epam.ta.reportportal.core.item.identity.UniqueIdGenerator;
import com.epam.ta.reportportal.core.item.impl.rerun.RerunSearcher;
import com.epam.ta.reportportal.core.item.impl.retry.RetryHandler;
import com.epam.ta.reportportal.core.item.validator.ParentItemValidator;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.TestCaseIdEntry;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PARENT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_TEST_CASE_HASH;
import static com.epam.ta.reportportal.ws.converter.converters.ItemAttributeConverter.TO_LAUNCH_ATTRIBUTE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class RerunHandlerImpl implements RerunHandler {

	private final TestItemRepository testItemRepository;
	private final LaunchRepository launchRepository;
	private final UniqueIdGenerator uniqueIdGenerator;
	private final TestCaseHashGenerator testCaseHashGenerator;
	private final ApplicationEventPublisher eventPublisher;
	private final RerunSearcher rerunSearcher;
	private final List<ParentItemValidator> parentItemValidators;
	private final RetryHandler retryHandler;

	@Autowired
	public RerunHandlerImpl(TestItemRepository testItemRepository, LaunchRepository launchRepository, UniqueIdGenerator uniqueIdGenerator,
			TestCaseHashGenerator testCaseHashGenerator, ApplicationEventPublisher eventPublisher, RerunSearcher rerunSearcher,
			List<ParentItemValidator> parentItemValidators, RetryHandler retryHandler) {
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
		this.uniqueIdGenerator = uniqueIdGenerator;
		this.testCaseHashGenerator = testCaseHashGenerator;
		this.eventPublisher = eventPublisher;
		this.rerunSearcher = rerunSearcher;
		this.parentItemValidators = parentItemValidators;
		this.retryHandler = retryHandler;
	}

	@Override
	public Launch handleLaunch(StartLaunchRQ request, Long projectId, ReportPortalUser user) {
		Optional<Launch> launchOptional = StringUtils.isEmpty(request.getRerunOf()) ?
				launchRepository.findLatestByNameAndProjectId(request.getName(), projectId) :
				launchRepository.findByUuid(request.getRerunOf());
		Launch launch = launchOptional.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND,
				ofNullable(request.getRerunOf()).orElse(request.getName())
		));

		ofNullable(request.getMode()).map(it -> LaunchModeEnum.valueOf(it.name())).ifPresent(launch::setMode);
		ofNullable(request.getDescription()).ifPresent(launch::setDescription);
		launch.setStatus(StatusEnum.IN_PROGRESS);
		ofNullable(request.getAttributes()).map(it -> it.stream()
				.map(attr -> TO_LAUNCH_ATTRIBUTE.apply(attr, launch))
				.collect(Collectors.toSet())).ifPresent(launch::setAttributes);
		ofNullable(request.getUuid()).ifPresent(launch::setUuid);
		launch.setRerun(true);
		return launch;
	}

	@Override
	public Optional<ItemCreatedRS> handleRootItem(StartTestItemRQ request, Launch launch) {
		final Integer testCaseHash = getTestCaseHash(request, launch);
		final Filter parentItemFilter = getRootItemFilter(launch, testCaseHash, request.getName());
		return rerunSearcher.findItem(parentItemFilter).flatMap(testItemRepository::findById).map(it -> updateRootItem(request, it));
	}

	private Integer getTestCaseHash(StartTestItemRQ request, Launch launch) {
		final TestCaseIdEntry testCaseIdEntry = TestItemBuilder.processTestCaseId(request);
		return ofNullable(testCaseIdEntry.getId()).map(id -> testCaseIdEntry.getHash()).orElseGet(() -> {
			TestItem newItem = new TestItemBuilder().addStartItemRequest(request).get();
			return testCaseHashGenerator.generate(newItem, Collections.emptyList(), launch.getProjectId());
		});
	}

	@Override
	public Optional<ItemCreatedRS> handleChildItem(StartTestItemRQ request, Launch launch, String parentUuid) {
		if (!request.isHasStats()) {
			return Optional.empty();
		}

		final Pair<Long, String> pathName = testItemRepository.selectPath(parentUuid)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, parentUuid));

		TestItem newItem = new TestItemBuilder().addLaunchId(launch.getId())
				.addStartItemRequest(request)
				.addAttributes(request.getAttributes())
				.addParentId(pathName.getFirst())
				.get();

		if (Objects.isNull(newItem.getTestCaseId())) {
			newItem.setTestCaseHash(testCaseHashGenerator.generate(newItem,
					IdentityUtil.getItemTreeIds(pathName.getSecond()),
					launch.getProjectId()
			));
		}

		final Filter childItemFilter = getChildItemFilter(launch, newItem.getTestCaseHash(), pathName.getFirst());

		return rerunSearcher.findItem(childItemFilter).flatMap(testItemRepository::findById).flatMap(foundItem -> {
			if (!foundItem.isHasChildren()) {
				final TestItem parent = testItemRepository.findIdByUuidForUpdate(parentUuid)
						.map(testItemRepository::getOne)
						.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, parentUuid));
				parentItemValidators.forEach(v -> v.validate(request, parent));
				return Optional.of(handleRetry(launch, newItem, foundItem, parent));
			}

			if (foundItem.getName().equals(newItem.getName())) {
				return Optional.of(updateRootItem(request, foundItem));
			}

			childItemFilter.withCondition(new FilterCondition(Condition.EQUALS, false, newItem.getName(), CRITERIA_NAME));
			return rerunSearcher.findItem(childItemFilter).flatMap(testItemRepository::findById).map(it -> updateRootItem(request, it));
		});
	}

	private Filter getCommonFilter(Long launchId, Integer testCaseHash) {
		return Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(new FilterCondition(Condition.EQUALS, false, String.valueOf(launchId), CRITERIA_LAUNCH_ID))
				.withCondition(new FilterCondition(Condition.EQUALS, false, String.valueOf(testCaseHash), CRITERIA_TEST_CASE_HASH))
				.build();
	}

	private Filter getRootItemFilter(Launch launch, Integer testCaseHash, String name) {
		return getCommonFilter(launch.getId(), testCaseHash).withCondition(new FilterCondition(Condition.EQUALS,
				false,
				name,
				CRITERIA_NAME
		)).withCondition(new FilterCondition(Condition.EXISTS, true, "1", CRITERIA_PARENT_ID));
	}

	private Filter getChildItemFilter(Launch launch, Integer testCaseHash, Long parentId) {
		return getCommonFilter(launch.getId(), testCaseHash).withCondition(new FilterCondition(Condition.EXISTS,
				false,
				String.valueOf(parentId),
				CRITERIA_PARENT_ID
		));
	}

	private ItemCreatedRS handleRetry(Launch launch, TestItem newItem, TestItem foundItem, TestItem parentItem) {
		eventPublisher.publishEvent(ItemRetryEvent.of(launch.getProjectId(), launch.getId(), foundItem.getItemId()));
		testItemRepository.save(newItem);
		newItem.setPath(parentItem.getPath() + "." + newItem.getItemId());
		generateUniqueId(launch, newItem);
		retryHandler.handleRetries(launch, newItem, foundItem.getItemId());
		return new ItemCreatedRS(newItem.getUuid(), newItem.getUniqueId());
	}

	private void generateUniqueId(Launch launch, TestItem item) {
		if (null == item.getUniqueId()) {
			item.setUniqueId(uniqueIdGenerator.generate(item, IdentityUtil.getParentIds(item), launch));
		}
	}

	private ItemCreatedRS updateRootItem(StartTestItemRQ request, TestItem foundItem) {
		foundItem = new TestItemBuilder(foundItem).addDescription(request.getDescription())
				.overwriteAttributes(request.getAttributes())
				.addStatus(StatusEnum.IN_PROGRESS)
				.get();
		ofNullable(request.getUuid()).ifPresent(foundItem::setUuid);
		return new ItemCreatedRS(foundItem.getUuid(), foundItem.getUniqueId());
	}
}
