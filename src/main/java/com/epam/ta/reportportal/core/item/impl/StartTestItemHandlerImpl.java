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
import com.epam.ta.reportportal.core.item.identity.IdentityUtil;
import com.epam.ta.reportportal.core.item.identity.TestCaseHashGenerator;
import com.epam.ta.reportportal.core.item.identity.UniqueIdGenerator;
import com.epam.ta.reportportal.core.item.impl.retry.RetriesHandler;
import com.epam.ta.reportportal.core.launch.rerun.RerunHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.isNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;
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

	private final UniqueIdGenerator uniqueIdGenerator;

	private final TestCaseHashGenerator testCaseHashGenerator;

	private final RerunHandler rerunHandler;

	private final RetriesHandler retriesHandler;

	@Autowired
	public StartTestItemHandlerImpl(TestItemRepository testItemRepository, LaunchRepository launchRepository,
			UniqueIdGenerator uniqueIdGenerator, TestCaseHashGenerator testCaseHashGenerator, RerunHandler rerunHandler,
			RetriesHandler retriesHandler) {
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
		this.uniqueIdGenerator = uniqueIdGenerator;
		this.testCaseHashGenerator = testCaseHashGenerator;
		this.rerunHandler = rerunHandler;
		this.retriesHandler = retriesHandler;
	}

	@Override
	@Transactional
	public ItemCreatedRS startRootItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq) {
		Launch launch = launchRepository.findByUuid(rq.getLaunchUuid())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, rq.getLaunchUuid()));
		validate(user, projectDetails, rq, launch);

		if (launch.isRerun()) {
			Optional<ItemCreatedRS> rerunCreatedRs = rerunHandler.handleRootItem(rq, launch);
			if (rerunCreatedRs.isPresent()) {
				return rerunCreatedRs.get();
			}
		}

		//		final TestItem testItem = testItemRepository.findByUuid("b8ee221c-69b6-461d-bee4-51da634ddc7b").orElseThrow();
		//
		//		System.out.println(testItem.isHasChildren());
		//		System.out.println(testItem.isHasStats());
		//
		//		testItem.setHasChildren(false);
		//
		//		final TestItem testItem1 = testItemRepository.findByUuid("b8ee221c-69b6-461d-bee4-51da634ddc7b").orElseThrow();
		//
		//		System.out.println(testItem1.isHasChildren());
		//		System.out.println(testItem1.isHasStats());

		TestItem item = new TestItemBuilder().addStartItemRequest(rq).addAttributes(rq.getAttributes()).addLaunchId(launch.getId()).get();
		testItemRepository.save(item);
		//		try {
		//			Thread.sleep(10000L);
		//		} catch (InterruptedException e) {
		//			e.printStackTrace();
		//		}
		//		final TestItem testItem12 = testItemRepository.findByUuidForUpdate("b8ee221c-69b6-461d-bee4-51da634ddc7b").orElseThrow();
		//		System.out.println(testItem12.getName());
		generateUniqueId(launch, item, String.valueOf(item.getItemId()));

		LOGGER.debug("Created new root TestItem {}", item.getUuid());
		return new ItemCreatedRS(item.getUuid(), item.getUniqueId());
	}

	@Override
	@Transactional
	public ItemCreatedRS startChildItem(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq,
			String parentId) {
		boolean isRetry = BooleanUtils.toBoolean(rq.isRetry()) || StringUtils.isNotBlank(rq.getRetryOf());

		Launch launch = launchRepository.findByUuid(rq.getLaunchUuid())
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, rq.getLaunchUuid()));

		final TestItem parentItem = testItemRepository.findByUuidForUpdate(parentId).orElseThrow();
//		try {
//			parentItem = CompletableFuture.supplyAsync(() -> {
//				return testItemRepository.findByUuid(parentId).orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, parentId));
//			}).get();
//		} catch (InterruptedException | ExecutionException e) {
//			throw new RuntimeException();
//		}

		validate(rq, parentItem);

		if (launch.isRerun()) {
			Optional<ItemCreatedRS> rerunCreatedRs = rerunHandler.handleChildItem(rq, launch, parentItem);
			if (rerunCreatedRs.isPresent()) {
				return rerunCreatedRs.get();
			}
		}

		TestItem item = new TestItemBuilder().addStartItemRequest(rq).addAttributes(rq.getAttributes()).addLaunchId(launch.getId()).get();

		if (isRetry) {

			//			Supplier<Optional<TestItem>> supplier = ofNullable(rq.getRetryOf()).map(retryOf -> (Supplier<Optional<TestItem>>) () -> testItemRepository
			//					.findByUuid(retryOf))
			//					.orElseGet(() -> () -> testItemRepository.findLatestByUniqueIdAndLaunchIdAndParentIdAndItemIdNotEqual(item.getUniqueId(),
			//							launch.getId(),
			//							parentItem.getItemId(),
			//							item.getItemId()
			//					));

			ofNullable(rq.getRetryOf()).ifPresentOrElse(retryOf -> {
				item.setParent(parentItem);
				testItemRepository.save(item);
				generateUniqueId(launch, item, parentItem.getPath() + "." + item.getItemId());
				retriesHandler.handleRetries(launch, item, retryOf);
			}, () -> {
				if (Objects.isNull(item.getTestCaseHash())) {
					item.setTestCaseHash(testCaseHashGenerator.generate(item,
							IdentityUtil.getItemTreeIds(parentItem),
							launch.getProjectId()
					));
				}
				testItemRepository.findLatestByTestCaseHashAndLaunchIdAndParentId(item.getTestCaseHash(),
						launch.getId(),
						parentItem.getItemId()
				).ifPresentOrElse(latest -> {
							item.setParent(parentItem);
							testItemRepository.save(item);
							generateUniqueId(launch, item, parentItem.getPath() + "." + item.getItemId());
							retriesHandler.handleRetries(launch, item, latest.getUuid());
						},
						() -> testItemRepository.findByUuidForUpdate(parentItem.getUuid())
								.ifPresent(parent -> testItemRepository.findLatestByTestCaseHashAndLaunchIdAndParentId(item.getTestCaseHash(),
										launch.getId(),
										parentItem.getItemId()
								).ifPresentOrElse(latest -> {
									item.setParent(parent);
									testItemRepository.save(item);
									generateUniqueId(launch, item, parentItem.getPath() + "." + item.getItemId());
									retriesHandler.handleRetries(launch, item, latest.getUuid());
								}, () -> {
									item.setParent(parent);
									testItemRepository.save(item);
									generateUniqueId(launch, item, parentItem.getPath() + "." + item.getItemId());
								}))
				);
			});

		} else {
			item.setParent(parentItem);
			testItemRepository.save(item);
			generateUniqueId(launch, item, parentItem.getPath() + "." + item.getItemId());
		}

		//		TestItem ppp = testItemRepository.findByUuidForUpdate("b8ee221c-69b6-461d-bee4-51da634ddc7b")
		//				.orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, parentId));
		//		ppp.setHasChildren(true);
		//
		//		System.out.println("HELLO: " + ppp.getName());
		//
		//		try {
		//			Thread.sleep(15000L);
		//		} catch (InterruptedException e) {
		//			e.printStackTrace();
		//		}
		//
		//		System.out.println("ready");

		LOGGER.debug("Created new child TestItem {} with root {}", item.getUuid(), parentId);

		if (rq.isHasStats() && !parentItem.isHasChildren()) {
			parentItem.setHasChildren(true);
		}

		return new ItemCreatedRS(item.getUuid(), item.getUniqueId());
	}

	/**
	 * Generates and sets {@link TestItem#getUniqueId()} and {@link TestItem#getTestCaseId()} if they are empty
	 *
	 * @param launch {@link Launch} of {@link TestItem}
	 * @param item   {@link TestItem}
	 * @param path   {@link TestItem} path
	 */
	private void generateUniqueId(Launch launch, TestItem item, String path) {
		item.setPath(path);
		if (Objects.isNull(item.getUniqueId())) {
			item.setUniqueId(uniqueIdGenerator.generate(item, IdentityUtil.getParentIds(item), launch));
		}
		if (Objects.isNull(item.getTestCaseId())) {
			item.setTestCaseHash(testCaseHashGenerator.generate(item, IdentityUtil.getParentIds(item), launch.getProjectId()));
		}
	}

	/**
	 * Validate {@link ReportPortalUser} credentials, {@link Launch#getStatus()}
	 * and {@link Launch} affiliation to the {@link com.epam.ta.reportportal.entity.project.Project}
	 *
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param rq             {@link StartTestItemRQ}
	 * @param launch         {@link Launch}
	 */
	private void validate(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq, Launch launch) {
		if (!UserRole.ADMINISTRATOR.equals(user.getUserRole())) {
			expect(projectDetails.getProjectId(), equalTo(launch.getProjectId())).verify(ACCESS_DENIED);
		}
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
					).get()
			);
		}

		if (rq.isHasStats()) {
			expect(parent.getRetryOf(), isNull()::test).verify(UNABLE_TO_SAVE_CHILD_ITEM_FOR_THE_RETRY, parent.getItemId());
		}

		expect(rq.getStartTime(), Preconditions.sameTimeOrLater(parent.getStartTime())).verify(CHILD_START_TIME_EARLIER_THAN_PARENT,
				rq.getStartTime(),
				parent.getStartTime(),
				parent.getItemId()
		);
	}
}
