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
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.LaunchStartedEvent;
import com.epam.ta.reportportal.core.events.item.ItemRetryEvent;
import com.epam.ta.reportportal.core.item.identity.IdentityUtil;
import com.epam.ta.reportportal.core.item.identity.TestCaseHashGenerator;
import com.epam.ta.reportportal.core.item.identity.UniqueIdGenerator;
import com.epam.ta.reportportal.core.item.impl.retry.RetriesHandler;
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
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.converter.converters.ItemAttributeConverter.TO_LAUNCH_ATTRIBUTE;
import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_ACTIVITY_RESOURCE;
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
	private final MessageBus messageBus;
	private final ApplicationEventPublisher eventPublisher;
	private final RetriesHandler retriesHandler;

	@Autowired
	public RerunHandlerImpl(TestItemRepository testItemRepository, LaunchRepository launchRepository, UniqueIdGenerator uniqueIdGenerator,
			TestCaseHashGenerator testCaseHashGenerator, MessageBus messageBus, ApplicationEventPublisher eventPublisher,
			RetriesHandler retriesHandler) {
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
		this.uniqueIdGenerator = uniqueIdGenerator;
		this.testCaseHashGenerator = testCaseHashGenerator;
		this.messageBus = messageBus;
		this.eventPublisher = eventPublisher;
		this.retriesHandler = retriesHandler;
	}

	@Override
	public StartLaunchRS handleLaunch(StartLaunchRQ request, Long projectId, ReportPortalUser user) {
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

		messageBus.publishActivity(new LaunchStartedEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId(), user.getUsername()));

		StartLaunchRS response = new StartLaunchRS();
		response.setId(launch.getUuid());
		response.setNumber(launch.getNumber());
		return response;
	}

	@Override
	public Optional<ItemCreatedRS> handleRootItem(StartTestItemRQ request, Launch launch) {
		final TestCaseIdEntry testCaseIdEntry = TestItemBuilder.processTestCaseId(request);
		return ofNullable(testCaseIdEntry.getId()).map(testCaseId -> updateRootItem(testCaseIdEntry.getHash(), request, launch))
				.orElseGet(() -> {
					TestItem newItem = new TestItemBuilder().addStartItemRequest(request).get();
					final Integer testCaseHash = testCaseHashGenerator.generate(newItem, Collections.emptyList(), launch.getProjectId());
					return updateRootItem(testCaseHash, request, launch);
				});
	}

	private Optional<ItemCreatedRS> updateRootItem(Integer testCaseHash, StartTestItemRQ request, Launch launch) {
		return testItemRepository.findLatestByTestCaseHashAndLaunchIdWithoutParents(testCaseHash, launch.getId())
				.map(foundItem -> updateRootItem(request, foundItem));
	}

	private ItemCreatedRS updateRootItem(StartTestItemRQ request, TestItem foundItem) {
		foundItem = new TestItemBuilder(foundItem).addDescription(request.getDescription())
				.overwriteAttributes(request.getAttributes())
				.addStatus(StatusEnum.IN_PROGRESS)
				.get();
		ofNullable(request.getUuid()).ifPresent(foundItem::setUuid);
		return new ItemCreatedRS(foundItem.getUuid(), foundItem.getUniqueId());
	}

	@Override
	public Optional<ItemCreatedRS> handleChildItem(StartTestItemRQ request, Launch launch, TestItem parent) {
		if (!request.isHasStats()) {
			return Optional.empty();
		}

		TestItem newItem = new TestItemBuilder().addLaunchId(launch.getId())
				.addStartItemRequest(request)
				.addAttributes(request.getAttributes())
				.addParent(parent)
				.get();
		if (Objects.isNull(newItem.getTestCaseId())) {
			newItem.setTestCaseHash(testCaseHashGenerator.generate(newItem, IdentityUtil.getItemTreeIds(parent), launch.getProjectId()));
		}
		return testItemRepository.findLatestByTestCaseHashAndLaunchIdAndParentId(newItem.getTestCaseHash(),
				launch.getId(),
				parent.getItemId()
		)
				.map(foundItem -> foundItem.isHasChildren() ?
						updateRootItem(request, foundItem) :
						handleRetry(launch, newItem, foundItem, parent));

	}

	private ItemCreatedRS handleRetry(Launch launch, TestItem newItem, TestItem foundItem, TestItem parentItem) {
		eventPublisher.publishEvent(ItemRetryEvent.of(launch.getProjectId(), launch.getId(), foundItem.getItemId()));
		testItemRepository.save(newItem);
		newItem.setPath(parentItem.getPath() + "." + newItem.getItemId());
		generateUniqueId(launch, newItem);
		retriesHandler.handleRetries(launch, newItem, foundItem.getUuid());
		return new ItemCreatedRS(newItem.getUuid(), newItem.getUniqueId());
	}

	private void generateUniqueId(Launch launch, TestItem item) {
		if (null == item.getUniqueId()) {
			item.setUniqueId(uniqueIdGenerator.generate(item, IdentityUtil.getParentIds(item), launch));
		}
	}
}
