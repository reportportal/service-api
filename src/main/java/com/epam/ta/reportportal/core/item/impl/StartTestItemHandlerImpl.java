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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.item.UniqueIdGenerator;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Start Test Item operation default implementation
 *
 * @author Andrei Varabyeu
 * @author Pavel Bortnik
 */
@Service
class StartTestItemHandlerImpl implements StartTestItemHandler {

	private TestItemRepository testItemRepository;

	private LaunchRepository launchRepository;

	private LogRepository logRepository;

	private UniqueIdGenerator identifierGenerator;

	private RabbitTemplate rabbitTemplate;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Autowired
	public void setLogRepository(LogRepository logRepository) {
		this.logRepository = logRepository;
	}

	@Autowired
	public void setIdentifierGenerator(UniqueIdGenerator identifierGenerator) {
		this.identifierGenerator = identifierGenerator;
	}

	@Autowired
	public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public ItemCreatedRS startRootItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq) {
		Launch launch = launchRepository.findById(rq.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, rq.getLaunchId().toString()));
		validate(user, projectDetails, rq, launch);
		TestItem item = new TestItemBuilder().addStartItemRequest(rq).addAttributes(rq.getAttributes()).addLaunch(launch).get();
		testItemRepository.save(item);

		item.setPath(String.valueOf(item.getItemId()));
		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item, launch));
		}

		return new ItemCreatedRS(item.getItemId(), item.getUniqueId());
	}

	@Override
	public ItemCreatedRS startChildItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq,
			Long parentId) {
		TestItem parentItem = testItemRepository.findById(parentId)
				.orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, parentId.toString()));
		Launch launch = launchRepository.findById(rq.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, rq.getLaunchId()));
		validate(rq, parentItem);

		//TODO retries
		TestItem item = new TestItemBuilder().addStartItemRequest(rq)
				.addAttributes(rq.getAttributes())
				.addLaunch(launch)
				.addParent(parentItem)
				.get();
		testItemRepository.save(item);
		item.setPath(parentItem.getPath() + "." + item.getItemId());
		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item, launch));
		}
		if (BooleanUtils.toBoolean(rq.isRetry())) {
			testItemRepository.handleRetries(item.getItemId());
		}
		return new ItemCreatedRS(item.getItemId(), item.getUniqueId());
	}

	/**
	 * Validate {@link ReportPortalUser} credentials, {@link Launch#status}
	 * and {@link Launch} affiliation to the {@link com.epam.ta.reportportal.entity.project.Project}
	 *
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @param rq             {@link StartTestItemRQ}
	 * @param launch         {@link Launch}
	 */
	private void validate(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq, Launch launch) {
		expect(projectDetails.getProjectId(), equalTo(launch.getProjectId())).verify(ACCESS_DENIED);
		expect(launch.getStatus(), equalTo(StatusEnum.IN_PROGRESS)).verify(START_ITEM_NOT_ALLOWED,
				formattedSupplier("Launch '{}' is not in progress", rq.getLaunchId())
		);
		expect(rq.getStartTime(), Preconditions.sameTimeOrLater(launch.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(),
				launch.getStartTime(),
				launch.getId()
		);
	}

	/**
	 * Verifies if the start of a child item is allowed. Conditions are
	 * - the item's start time must be same or later than the parent's
	 * - the parent item must be in progress
	 * - the parent item hasn't any logs
	 *
	 * @param rq     Start child item request
	 * @param parent Parent item
	 */
	private void validate(StartTestItemRQ rq, TestItem parent) {
		expect(rq.getStartTime(), Preconditions.sameTimeOrLater(parent.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(),
				parent.getStartTime(),
				parent.getItemId()
		);
		expect(parent.getItemResults().getStatus(), Preconditions.statusIn(StatusEnum.IN_PROGRESS)).verify(START_ITEM_NOT_ALLOWED,
				formattedSupplier("Parent Item '{}' is not in progress", parent.getItemId())
		);
		expect(logRepository.hasLogs(parent.getItemId()), equalTo(false)).verify(START_ITEM_NOT_ALLOWED,
				formattedSupplier("Parent Item '{}' already has log items", parent.getItemId())
		);
	}
}
