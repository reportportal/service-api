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

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.Preconditions;
import com.epam.ta.reportportal.store.database.dao.LaunchRepository;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.item.TestItemStructure;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.store.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Start Launch operation default implementation
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
@Service
class StartTestItemHandlerImpl implements StartTestItemHandler {

	private TestItemRepository testItemRepository;

	private LaunchRepository launchRepository;

	private UniqueIdGenerator identifierGenerator;

	@Autowired
	public void setIdentifierGenerator(UniqueIdGenerator identifierGenerator) {
		this.identifierGenerator = identifierGenerator;
	}

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	/**
	 * Starts root item and related to the specific launch
	 */
	@Override
	public ItemCreatedRS startRootItem(String projectName, StartTestItemRQ rq) {
		Launch launch = launchRepository.findById(rq.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, rq.getLaunchId().toString()));
		validate(projectName, rq, launch);

		TestItem item = new TestItemBuilder().addStartItemRequest(rq).get();
		TestItemStructure testItemStructure = new TestItemStructure();
		testItemStructure.setLaunch(launch);

		item.setTestItemStructure(testItemStructure);

		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item, launch));
		}

		testItemRepository.save(item);
		testItemRepository.refresh(item);
		return new ItemCreatedRS(item.getItemId(), item.getUniqueId());
	}

	/**
	 * Starts children item and building it's path from parent with parent's
	 */
	@Override
	public ItemCreatedRS startChildItem(String projectName, StartTestItemRQ rq, Long parentId) {
		TestItem parentItem = testItemRepository.findById(parentId)
				.orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, parentId.toString()));

		Launch launch = launchRepository.findById(rq.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, rq.getLaunchId()));

		validate(parentItem, parentId);
		validate(rq, parentItem);

		TestItem item = new TestItemBuilder().addStartItemRequest(rq).get();
		TestItemStructure testItemStructure = new TestItemStructure();
		testItemStructure.setLaunch(launch);
		testItemStructure.setParent(parentItem.getTestItemStructure());

		item.setTestItemStructure(testItemStructure);

		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item, launch));
		}

		//		if (rq.isRetry()) {
		//			TestItem retryRoot = getRetryRoot(item.getUniqueId(), parent);
		//			if (null == retryRoot.getRetryProcessed()) {
		//				retryRoot.setRetryProcessed(false);
		//				testItemRepository.partialUpdate(retryRoot);
		//			}
		//
		//			item.setRetryProcessed(false);
		//			testItemRepository.save(item);
		//			launchRepository.updateHasRetries(item.getLaunchRef(), true);
		//
		//		} else {
		//			LOGGER.debug("Starting Item with name '{}'", item.getName());
		//			testItemRepository.save(item);
		//			if (!parentItem.hasChilds()) {
		//				testItemRepository.updateHasChilds(parentItem.getId(), true);
		//			}
		//		}

		testItemRepository.save(item);
		testItemRepository.refresh(item);
		return new ItemCreatedRS(item.getItemId(), item.getUniqueId());
	}

	private void validate(String projectName, StartTestItemRQ rq, Launch launch) {
		//expect(projectName.toLowerCase(), equalTo(launch.getProjectRef())).verify(ACCESS_DENIED);
		//		expect(launch, Preconditions.IN_PROGRESS).verify(START_ITEM_NOT_ALLOWED,
		//				Suppliers.formattedSupplier("Launch '{}' is not in progress", rq.getLaunchId())
		//		);

		expect(launch.getStatus(), equalTo(StatusEnum.IN_PROGRESS)).verify(START_ITEM_NOT_ALLOWED,
				Suppliers.formattedSupplier("Launch '{}' is not in progress", rq.getLaunchId())
		);

		expect(rq, Preconditions.startSameTimeOrLater(launch.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(), launch.getStartTime(), launch.getId()
		);
		expect(rq.isRetry(), equalTo(false)).verify(BAD_REQUEST_ERROR, "Root test item can't be a retry.");

	}

	private void validate(TestItem parentTestItem, Long parent) {
		//		expect(parentTestItem.getTestItemResults(), Objects::nonNull).verify(START_ITEM_NOT_ALLOWED,
		//				Suppliers.formattedSupplier("Parent Item '{}' is not in progress", parentTestItem.getId())
		//		);
		//		long logCount = logRepository.getNumberOfLogByTestItem(parentTestItem);
		//		expect(logCount, equalTo(0L)).verify(START_ITEM_NOT_ALLOWED,
		//				Suppliers.formattedSupplier("Parent Item '{}' already has log items", parentTestItem.getId()));
	}

	private void validate(StartTestItemRQ rq, TestItem parent) {
		expect(rq, Preconditions.startSameTimeOrLater(parent.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(), parent.getStartTime(), parent.getItemId()
		);
	}
}
