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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.util.RetryId;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.util.List;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Start Launch operation default implementation
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
@Service
class StartTestItemHandlerImpl implements StartTestItemHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(StartTestItemHandlerImpl.class);

	private TestItemRepository testItemRepository;
	private LaunchRepository launchRepository;
	private Provider<TestItemBuilder> testItemBuilder;
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

	@Autowired
	public void setTestItemBuilder(Provider<TestItemBuilder> testItemBuilder) {
		this.testItemBuilder = testItemBuilder;
	}

	/**
	 * Starts root item and related to the specific launch
	 */
	@Override
	public ItemCreatedRS startRootItem(String projectName, StartTestItemRQ rq) {
		Launch launch = launchRepository.loadStatusProjectRefAndStartTime(rq.getLaunchId());
		validate(projectName, rq, launch);
		TestItem item = testItemBuilder.get().addStartItemRequest(rq).addStatus(Status.IN_PROGRESS).addLaunch(launch).build();
		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item));
		}
		testItemRepository.save(item);
		return new ItemCreatedRS(item.getId(), item.getUniqueId());
	}

	/**
	 * Starts children item and building it's path from parent with parant's
	 */
	@Override
	public ItemCreatedRS startChildItem(String projectName, StartTestItemRQ rq, String parent) {
		TestItem parentItem = testItemRepository.findOne(parent);

		validate(parentItem, parent);
		validate(rq, parentItem);

		TestItem item = testItemBuilder.get()
				.addStartItemRequest(rq)
				.addParent(parentItem)
				.addPath(parentItem)
				.addStatus(Status.IN_PROGRESS)
				.build();

		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item));
		}

		if (rq.isRetry()) {
			TestItem retryRoot = getRetryRoot(item.getUniqueId(), parent);

			RetryId retryId = RetryId.newID(retryRoot.getId());

			item.setId(retryId.toString());
			item.setParent(null);

			LOGGER.debug("Adding retry with ID '{}' to item '{}'", item.getId(), retryRoot.getId());
			testItemRepository.addRetry(retryRoot.getId(), item);

		} else {
			testItemRepository.save(item);

			if (!parentItem.hasChilds()) {
				testItemRepository.updateHasChilds(parentItem.getId(), true);
			}
		}

		return new ItemCreatedRS(item.getId(), item.getUniqueId());
	}

	@VisibleForTesting
	TestItem getRetryRoot(String uniqueID, String parent) {
		LOGGER.info("Looking for retry root. Parent: {}. Unique ID: {}", parent, uniqueID);

		RetryTemplate rt = new RetryTemplate();
		rt.setRetryPolicy(new SimpleRetryPolicy(10));
		rt.setThrowLastExceptionOnExhausted(true);

		TestItem retryRoot = rt.execute(context -> {
			List<TestItem> retryItems = testItemRepository.findByUniqueId(uniqueID, parent);
			BusinessRule.expect(retryItems, Preconditions.NOT_EMPTY_COLLECTION)
					.verify(ErrorType.INCORRECT_REQUEST, "Unable to find retry root");

			LOGGER.info("Found {} retry root candidates", retryItems.size());
			TestItem item;

			//first retry of some test item
			if (1 == retryItems.size()) {
				item = retryItems.get(0);
			} else {
				//second retry. Make sure we take the one that already has retries
				item = retryItems.stream().filter(it -> null != it.getRetries()).findAny().get();
			}
			return item;
		});

		LOGGER.debug("Found retry root:" + retryRoot.getId());
		return retryRoot;
	}

	private void validate(String projectName, StartTestItemRQ rq, Launch launch) {
		expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, rq.getLaunchId());
		expect(projectName.toLowerCase(), equalTo(launch.getProjectRef())).verify(ACCESS_DENIED);
		expect(launch, Preconditions.IN_PROGRESS).verify(START_ITEM_NOT_ALLOWED,
				Suppliers.formattedSupplier("Launch '{}' is not in progress", rq.getLaunchId())
		);

		expect(rq, Preconditions.startSameTimeOrLater(launch.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(),
				launch.getStartTime(),
				launch.getId()
		);

	}

	private void validate(TestItem parentTestItem, String parent) {
		expect(parentTestItem, notNull()).verify(TEST_ITEM_NOT_FOUND, parent);
		expect(parentTestItem, Preconditions.IN_PROGRESS).verify(START_ITEM_NOT_ALLOWED,
				Suppliers.formattedSupplier("Parent Item '{}' is not in progress", parentTestItem.getId())
		);
		//		long logCount = logRepository.getNumberOfLogByTestItem(parentTestItem);
		//		expect(logCount, equalTo(0L)).verify(START_ITEM_NOT_ALLOWED,
		//				Suppliers.formattedSupplier("Parent Item '{}' already has log items", parentTestItem.getId()));
	}

	private void validate(StartTestItemRQ rq, TestItem parent) {
		expect(rq, Preconditions.startSameTimeOrLater(parent.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(),
				parent.getStartTime(),
				parent.getId()
		);
	}
}