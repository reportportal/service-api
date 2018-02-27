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

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.Preconditions;
import com.epam.ta.reportportal.store.database.dao.LaunchRepository;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.item.TestItemResults;
import com.epam.ta.reportportal.store.database.entity.item.TestItemStructure;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

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


	private static final Logger LOGGER = LoggerFactory.getLogger(StartTestItemHandlerImpl.class);

	private TestItemRepository testItemRepository;
	private LaunchRepository launchRepository;
	private UniqueIdGenerator identifierGenerator;
	//private RetryTemplate retrier;

	public StartTestItemHandlerImpl() {
		//		retrier = new RetryTemplate();
		//		TimeoutRetryPolicy timeoutRetryPolicy = new TimeoutRetryPolicy();
		//		timeoutRetryPolicy.setTimeout(TimeUnit.SECONDS.toMillis(3L));
		//		retrier.setRetryPolicy(timeoutRetryPolicy);
		//		retrier.setBackOffPolicy(new FixedBackOffPolicy());
		//		retrier.setThrowLastExceptionOnExhausted(true);
	}

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
		testItemStructure.setLaunchId(rq.getLaunchId());
		TestItemResults testItemResults = new TestItemResults();
		testItemResults.setStatus(StatusEnum.IN_PROGRESS);

		item.setTestItemStructure(testItemStructure);
		item.setTestItemResults(testItemResults);

		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item));
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

		validate(parentItem, parentId);
		validate(rq, parentItem);

		TestItem item = new TestItemBuilder().addStartItemRequest(rq).get();
		TestItemStructure testItemStructure = new TestItemStructure();
		testItemStructure.setLaunchId(rq.getLaunchId());
		testItemStructure.setParentId(parentId);
		TestItemResults testItemResults = new TestItemResults();
		testItemResults.setStatus(StatusEnum.IN_PROGRESS);

		item.setTestItemStructure(testItemStructure);
		item.setTestItemResults(testItemResults);

		if (null == item.getUniqueId()) {
			item.setUniqueId(identifierGenerator.generate(item));
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

	@VisibleForTesting
	TestItem getRetryRoot(String uniqueID, String parent) {
		throw new UnsupportedOperationException();
		//		LOGGER.warn("Looking for retry root. Parent: {}. Unique ID: {}", parent, uniqueID);
		//		/*
		//		 * Due to async nature of RP clients and some TestNG
		//		 * implementation details both 'start item' of root of retry and 'start item' events
		//		 * may come at the same time (almost). To simplify client side we don't introduce requirement
		//		 * that 1st retry item have to wait for the item that causes of retry (retry root). Instead, we
		//		 * just introduce some wait on server side. This case if extremely specific, in 99% real-world cases
		//		 * results will be returned from first attempt
		//		 */
		//		return retrier.execute(context -> {
		//			/* search for the item with the same unique ID and parent. Since retries do not contain
		//			 * parentID, there should be only one result. For correct further processing - it needs
		//			 * to be waited for the end of retries root item.
		//			 */
		//			TestItem item = testItemRepository.findRetryRoot(uniqueID, parent);
		//			BusinessRule.expect(item, com.epam.ta.reportportal.commons.Predicates.notNull())
		//					.verify(ErrorType.BAD_REQUEST_ERROR, "No retry root found");
		//			BusinessRule.expect(item.getStatus(), Predicates.not(it -> it.equals(Status.IN_PROGRESS)))
		//					.verify(BAD_REQUEST_ERROR, "Retries root is still IN_PROGRESS");
		//			return item;
		//		});
	}

	private void validate(String projectName, StartTestItemRQ rq, Launch launch) {
		expect(launch, Objects::nonNull).verify(LAUNCH_NOT_FOUND, rq.getLaunchId());
		//expect(projectName.toLowerCase(), equalTo(launch.getProjectRef())).verify(ACCESS_DENIED);
		//		expect(launch, Preconditions.IN_PROGRESS).verify(START_ITEM_NOT_ALLOWED,
		//				Suppliers.formattedSupplier("Launch '{}' is not in progress", rq.getLaunchId())
		//		);

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
