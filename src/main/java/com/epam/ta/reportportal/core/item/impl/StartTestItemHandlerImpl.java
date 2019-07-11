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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.item.UniqueIdGenerator;
import com.epam.ta.reportportal.core.launch.rerun.RerunHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.isNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

/**
 * Start Test Item operation default implementation
 *
 * @author Andrei Varabyeu
 * @author Pavel Bortnik
 */
@Service
@Primary
class StartTestItemHandlerImpl implements StartTestItemHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(StartTestItemHandlerImpl.class);

	private final TestItemRepository testItemRepository;

	private final LaunchRepository launchRepository;

	private final UniqueIdGenerator identifierGenerator;

	private final RerunHandler rerunHandler;

	@Autowired
	public StartTestItemHandlerImpl(TestItemRepository testItemRepository, LaunchRepository launchRepository,
			UniqueIdGenerator identifierGenerator, RerunHandler rerunHandler) {
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
		this.identifierGenerator = identifierGenerator;
		this.rerunHandler = rerunHandler;
	}

	@Override
	public ItemCreatedRS startRootItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq) {
		Launch launch = launchRepository.findByUuid(rq.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, rq.getLaunchId()));
		validate(user, projectDetails, rq, launch);

		if (launch.isRerun()) {
			Optional<ItemCreatedRS> rerunCreatedRs = rerunHandler.handleRootItem(rq, launch);
			if (rerunCreatedRs.isPresent()) {
				return rerunCreatedRs.get();
			}
		}

		TestItem item = new TestItemBuilder().addStartItemRequest(rq).addAttributes(rq.getAttributes()).addLaunch(launch).get();
		testItemRepository.save(item);
		generateUniqueId(launch, item, String.valueOf(item.getItemId()));

		LOGGER.debug("Created new root TestItem {}", item.getUuid());
		return new ItemCreatedRS(item.getItemId(), item.getUniqueId(), item.getUuid());
	}

	@Override
	public ItemCreatedRS startChildItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq,
			String parentId) {
		TestItem parentItem = testItemRepository.findByUuid(parentId)
				.orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, parentId));
		Launch launch = launchRepository.findByUuid(rq.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, rq.getLaunchId()));
		validate(rq, parentItem);

		if (launch.isRerun()) {
			Optional<ItemCreatedRS> rerunCreatedRs = rerunHandler.handleChildItem(rq, launch, parentItem);
			if (rerunCreatedRs.isPresent()) {
				return rerunCreatedRs.get();
			}
		}

		TestItem item = new TestItemBuilder().addStartItemRequest(rq)
				.addAttributes(rq.getAttributes())
				.addLaunch(launch)
				.addParent(parentItem)
				.get();

		testItemRepository.save(item);
		generateUniqueId(launch, item, parentItem.getPath() + "." + item.getItemId());
		if (rq.isHasStats() && !parentItem.isHasChildren()) {
			parentItem.setHasChildren(true);
		}
		if (BooleanUtils.toBoolean(rq.isRetry())) {
			handleRetries(launch, item);
		}

		LOGGER.debug("Created new child TestItem {} with root {}", item.getUuid(), parentId);
		return new ItemCreatedRS(item.getItemId(), item.getUniqueId(), item.getUuid());
	}

	/**
	 * Generates and sets unique ID to {@link TestItem} if it is empty
	 *
	 * @param launch {@link Launch} of {@link TestItem}
	 * @param item   {@link TestItem}
	 * @param path   {@link TestItem} path
	 */
	private void generateUniqueId(Launch launch, TestItem item, String path) {
		item.setPath(path);
		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item, launch));
		}
	}

	/**
	 * Handles retry items
	 *
	 * @param launch {@link Launch}
	 * @param item   {@link TestItem}
	 */
	private void handleRetries(Launch launch, TestItem item) {
		testItemRepository.handleRetries(item.getItemId());
		if (!launch.isHasRetries()) {
			launch.setHasRetries(launchRepository.hasRetries(launch.getId()));
		}
	}

	/**
	 * Validate {@link ReportPortalUser} credentials, {@link Launch#status}
	 * and {@link Launch} affiliation to the {@link com.epam.ta.reportportal.entity.project.Project}
	 *
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param rq             {@link StartTestItemRQ}
	 * @param launch         {@link Launch}
	 */
	private void validate(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq, Launch launch) {
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(projectDetails.getProjectId(), equalTo(launch.getProjectId())).verify(ACCESS_DENIED);
		}
		expect(launch.getStatus(), equalTo(StatusEnum.IN_PROGRESS)).verify(START_ITEM_NOT_ALLOWED,
				formattedSupplier("Launch '{}' is not in progress", rq.getLaunchId())
		);
		expect(rq.getStartTime(), Preconditions.sameTimeOrLater(launch.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(),
				launch.getStartTime(),
				launch.getId()
		);
		expect(isTrue(BooleanUtils.toBoolean(rq.isRetry())), equalTo(false)).verify(BAD_REQUEST_ERROR, "Root test item can't be a retry.");
	}

	/**
	 * Verifies if the start of a child item is allowed. Conditions are
	 * - the item's parent should not be a retry
	 * - the item's start time must be same or later than the parent's
	 * - the parent item hasn't any logs
	 *
	 * @param rq     Start child item request
	 * @param parent Parent item
	 */
	private void validate(StartTestItemRQ rq, TestItem parent) {

		if (!parent.isHasStats()) {
			expect(rq.isHasStats(), equalTo(Boolean.FALSE)).verify(ErrorType.BAD_REQUEST_ERROR,
					Suppliers.formattedSupplier("Unable to add a not nested step item, because parent item with ID = '{}' is a nested step",
							parent.getItemId()
					)
							.get()
			);
		}

		expect(parent.getRetryOf(), isNull()::test).verify(UNABLE_TO_SAVE_CHILD_ITEM_FOR_THE_RETRY, parent.getItemId());

		expect(rq.getStartTime(), Preconditions.sameTimeOrLater(parent.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(),
				parent.getStartTime(),
				parent.getItemId()
		);
	}
}
