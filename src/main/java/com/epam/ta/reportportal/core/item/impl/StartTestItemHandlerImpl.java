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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.item.UniqueIdGenerator;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.TestItemStructureRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Start Test Item operation default implementation
 *
 * @author Andrei Varabyeu
 * @author Pavel Bortnik
 */
@Service
class StartTestItemHandlerImpl implements StartTestItemHandler {

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private TestItemStructureRepository structureRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private UniqueIdGenerator identifierGenerator;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public ItemCreatedRS startRootItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq) {
		Launch launch = launchRepository.findById(rq.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, rq.getLaunchId().toString()));
		validate(user, projectDetails, rq, launch);
		TestItem item = new TestItemBuilder().addStartItemRequest(rq).addLaunch(launch).get();
		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item, launch));
		}
		structureRepository.save(item.getItemStructure());
		return new ItemCreatedRS(item.getItemId(), item.getUniqueId());
	}

	@Override
	public ItemCreatedRS startChildItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq, Long parentId) {
		TestItem parentItem = testItemRepository.findById(parentId)
				.orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, parentId.toString()));
		Launch launch = launchRepository.findById(rq.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, rq.getLaunchId()));
		validate(rq, parentItem);

		TestItem item = new TestItemBuilder().addStartItemRequest(rq).addLaunch(launch).addParent(parentItem.getItemStructure()).get();
		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item, launch));
		}
		//TODO retries
		structureRepository.save(item.getItemStructure());
		return new ItemCreatedRS(item.getItemId(), item.getUniqueId());
	}

	/**
	 * TODO document this
	 *
	 * @param user
	 * @param projectDetails
	 * @param rq
	 * @param launch
	 */
	private void validate(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq, Launch launch) {
		expect(projectDetails.getProjectId(), equalTo(launch.getProjectId())).verify(ACCESS_DENIED);
		expect(launch.getStatus(), equalTo(StatusEnum.IN_PROGRESS)).verify(START_ITEM_NOT_ALLOWED,
				formattedSupplier("Launch '{}' is not in progress", rq.getLaunchId())
		);
		expect(rq.getStartTime(), Preconditions.sameTimeOrLater(launch.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(), launch.getStartTime(), launch.getId()
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
				rq.getStartTime(), parent.getStartTime(), parent.getItemId()
		);
		expect(parent.getItemStructure().getItemResults().getStatus(), Preconditions.statusIn(StatusEnum.IN_PROGRESS)).verify(
				START_ITEM_NOT_ALLOWED, formattedSupplier("Parent Item '{}' is not in progress", parent.getItemId()));
		expect(logRepository.hasLogs(parent.getItemId()), equalTo(false)).verify(START_ITEM_NOT_ALLOWED,
				formattedSupplier("Parent Item '{}' already has log items", parent.getItemId())
		);
	}
}
