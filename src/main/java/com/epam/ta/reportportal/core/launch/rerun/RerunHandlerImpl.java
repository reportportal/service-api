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
import com.epam.ta.reportportal.core.item.identity.TestCaseHashGenerator;
import com.epam.ta.reportportal.core.item.identity.UniqueIdGenerator;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.ParameterResource;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.entity.enums.TestItemTypeEnum.STEP;
import static com.epam.ta.reportportal.ws.converter.converters.ItemAttributeConverter.TO_LAUNCH_ATTRIBUTE;
import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.converter.converters.ParametersConverter.TO_MODEL;
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

	@Autowired
	public RerunHandlerImpl(TestItemRepository testItemRepository, LaunchRepository launchRepository, UniqueIdGenerator uniqueIdGenerator,
			TestCaseHashGenerator testCaseHashGenerator, MessageBus messageBus, ApplicationEventPublisher eventPublisher) {
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
		this.uniqueIdGenerator = uniqueIdGenerator;
		this.testCaseHashGenerator = testCaseHashGenerator;
		this.messageBus = messageBus;
		this.eventPublisher = eventPublisher;
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
		Optional<TestItem> itemOptional = testItemRepository.findByNameAndLaunchWithoutParents(request.getName(), launch.getId());

		if (itemOptional.isEmpty() || isParametersNotEqual(request.getParameters(), itemOptional.get().getParameters())) {
			return Optional.empty();
		}

		TestItem item = handleRerun(request, launch, itemOptional.get(), null);
		return Optional.of(new ItemCreatedRS(item.getUuid(), item.getUniqueId()));
	}

	@Override
	public Optional<ItemCreatedRS> handleChildItem(StartTestItemRQ request, Launch launch, TestItem parent) {
		Optional<TestItem> itemOptional = testItemRepository.findByNameAndLaunchUnderPath(request.getName(),
				launch.getId(),
				parent.getPath()
		);

		if (itemOptional.isEmpty() || isParametersNotEqual(request.getParameters(), itemOptional.get().getParameters())) {
			return Optional.empty();
		}

		TestItem item = handleRerun(request, launch, itemOptional.get(), parent);
		return Optional.of(new ItemCreatedRS(item.getUuid(), item.getUniqueId()));
	}

	private boolean isParametersNotEqual(List<ParameterResource> fromRequest, Set<Parameter> stored) {
		Set<Parameter> requestParameters = ofNullable(fromRequest).map(it -> it.stream().map(TO_MODEL).collect(Collectors.toSet()))
				.orElse(Collections.emptySet());
		return !stored.equals(requestParameters);
	}

	private TestItem handleRerun(StartTestItemRQ request, Launch launch, TestItem testItem, TestItem parent) {
		TestItem item;
		item = testItem;
		item.setDescription(request.getDescription());
		if (item.getType().sameLevel(STEP)) {
			eventPublisher.publishEvent(ItemRetryEvent.of(launch.getProjectId(), launch.getId(), item.getItemId()));
			item = makeRetry(request, launch, parent);
		} else {
			item.getItemResults().setStatus(StatusEnum.IN_PROGRESS);
		}
		ofNullable(request.getUuid()).ifPresent(item::setUuid);
		return item;
	}

	private TestItem makeRetry(StartTestItemRQ request, Launch launch, TestItem parent) {
		TestItem retry = new TestItemBuilder().addLaunchId(launch.getId())
				.addStartItemRequest(request)
				.addAttributes(request.getAttributes())
				.addParent(parent)
				.get();
		testItemRepository.save(retry);
		generateUniqueId(launch,
				retry,
				ofNullable(parent).map(it -> it.getPath() + "." + retry.getItemId()).orElse(String.valueOf(retry.getItemId()))
		);
		handleRetries(launch, retry);
		return retry;
	}

	private void generateUniqueId(Launch launch, TestItem item, String path) {
		item.setPath(path);
		if (null == item.getUniqueId()) {
			item.setUniqueId(uniqueIdGenerator.generate(item, launch));
		}
		if (Objects.isNull(item.getTestCaseId())) {
			item.setTestCaseHash(testCaseHashGenerator.generate(item, launch.getProjectId()));
		}
	}

	private void handleRetries(Launch launch, TestItem item) {
		testItemRepository.handleRetries(item.getItemId());
		if (!launch.isHasRetries()) {
			launch.setHasRetries(launchRepository.hasRetries(launch.getId()));
		}
	}
}
