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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Start Launch operation default implementation
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 *
 */
@Service
class StartTestItemHandlerImpl implements StartTestItemHandler {
	private TestItemRepository testItemRepository;
	private LaunchRepository launchRepository;
	private Provider<TestItemBuilder> testItemBuilder;
	private LogRepository logRepository;
	private String projectName;

	@Autowired
	private UniqueIdGenerator identifierGenerator;

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
	public void setTestItemBuilder(Provider<TestItemBuilder> testItemBuilder) {
		this.testItemBuilder = testItemBuilder;
	}

	/**
	 * Starts root item and related to the specific launch
	 */
	@Override
	public EntryCreatedRS startRootItem(String projectName, StartTestItemRQ rq) {
		this.projectName = projectName;
		Launch launch = launchRepository.loadStatusProjectRefAndStartTime(rq.getLaunchId());
		validate(projectName, rq, launch);
		TestItem item = testItemBuilder.get().addStartItemRequest(rq).addStatus(Status.IN_PROGRESS).addLaunch(launch).build();
		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item, projectName));
		}
		testItemRepository.save(item);
		return new EntryCreatedRS(item.getId());
	}

	/**
	 * Starts children item and building it's path from parent with parant's
	 */
	@Override
	public EntryCreatedRS startChildItem(StartTestItemRQ rq, String parent) {
		TestItem parentItem = testItemRepository.findOne(parent);

		validate(parentItem, parent);
		validate(rq, parentItem);

		TestItem item = testItemBuilder.get().addStartItemRequest(rq).addParent(parentItem).addPath(parentItem)
				.addStatus(Status.IN_PROGRESS).build();
		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item, projectName));
		}
		testItemRepository.save(item);

		if (!parentItem.hasChilds()) {
			testItemRepository.updateHasChilds(parentItem.getId(), true);
		}
		return new EntryCreatedRS(item.getId());
	}

	private void validate(String projectName, StartTestItemRQ rq, Launch launch) {
		expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, rq.getLaunchId());
		expect(projectName.toLowerCase(), equalTo(launch.getProjectRef())).verify(ACCESS_DENIED);
		expect(launch, Preconditions.IN_PROGRESS).verify(START_ITEM_NOT_ALLOWED,
				Suppliers.formattedSupplier("Launch '{}' is not in progress", rq.getLaunchId()));

		expect(rq, Preconditions.startSameTimeOrLater(launch.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(), launch.getStartTime(), launch.getId());

	}

	private void validate(TestItem parentTestItem, String parent) {
		expect(parentTestItem, notNull()).verify(TEST_ITEM_NOT_FOUND, parent);
		expect(parentTestItem, Preconditions.IN_PROGRESS).verify(START_ITEM_NOT_ALLOWED,
				Suppliers.formattedSupplier("Parent Item '{}' is not in progress", parentTestItem.getId()));
		long logCount = logRepository.getNumberOfLogByTestItem(parentTestItem);
		expect(logCount, equalTo(0L)).verify(START_ITEM_NOT_ALLOWED,
				Suppliers.formattedSupplier("Parent Item '{}' already has log items", parentTestItem.getId()));
	}

	private void validate(StartTestItemRQ rq, TestItem parent) {
		expect(rq, Preconditions.startSameTimeOrLater(parent.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(), parent.getStartTime(), parent.getId());
	}
}