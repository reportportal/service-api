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

import com.epam.reportportal.events.ElementsDeletedEvent;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.ElementsCounterService;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.item.DeleteTestItemHandler;
import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.PathName;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Default implementation of {@link DeleteTestItemHandler}
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteTestItemHandlerImpl implements DeleteTestItemHandler {

	private final TestItemRepository testItemRepository;

	private final ContentRemover<Long> itemContentRemover;

	private final LogIndexer logIndexer;

	private final LaunchRepository launchRepository;

	private final AttachmentRepository attachmentRepository;

	private final ApplicationEventPublisher eventPublisher;

	private final ElementsCounterService elementsCounterService;

	@Autowired
	public DeleteTestItemHandlerImpl(TestItemRepository testItemRepository, ContentRemover<Long> itemContentRemover, LogIndexer logIndexer,
			LaunchRepository launchRepository, AttachmentRepository attachmentRepository, ApplicationEventPublisher eventPublisher,
			ElementsCounterService elementsCounterService) {
		this.testItemRepository = testItemRepository;
		this.itemContentRemover = itemContentRemover;
		this.logIndexer = logIndexer;
		this.launchRepository = launchRepository;
		this.attachmentRepository = attachmentRepository;
		this.eventPublisher = eventPublisher;
		this.elementsCounterService = elementsCounterService;
	}

	@Override
	public OperationCompletionRS deleteTestItem(Long itemId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		TestItem item = testItemRepository.findById(itemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
		Launch launch = launchRepository.findById(item.getLaunchId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, item.getLaunchId()));

		validate(item, launch, user, projectDetails);
		Optional<Long> parentId = ofNullable(item.getParentId());

		Set<Long> itemsForRemove = Sets.newHashSet(testItemRepository.selectAllDescendantsIds(item.getPath()));
		itemsForRemove.forEach(itemContentRemover::remove);

		eventPublisher.publishEvent(new ElementsDeletedEvent(item,
				projectDetails.getProjectId(),
				elementsCounterService.countNumberOfItemElements(item)
		));
		itemContentRemover.remove(item.getItemId());
		testItemRepository.deleteById(item.getItemId());

		launch.setHasRetries(launchRepository.hasRetries(launch.getId()));
		parentId.flatMap(testItemRepository::findById)
				.ifPresent(p -> p.setHasChildren(testItemRepository.hasChildren(p.getItemId(), p.getPath())));

		logIndexer.indexItemsRemoveAsync(projectDetails.getProjectId(), itemsForRemove);
		attachmentRepository.moveForDeletionByItems(itemsForRemove);

		return COMPOSE_DELETE_RESPONSE.apply(item.getItemId());
	}

	@Override
	public List<OperationCompletionRS> deleteTestItems(Collection<Long> ids, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		List<TestItem> items = testItemRepository.findAllById(ids);

		List<Launch> launches = launchRepository.findAllById(items.stream()
				.map(TestItem::getLaunchId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));
		Map<Long, List<TestItem>> launchItemMap = items.stream().collect(Collectors.groupingBy(TestItem::getLaunchId));
		launches.forEach(launch -> launchItemMap.get(launch.getId()).forEach(item -> validate(item, launch, user, projectDetails)));

		Map<Long, PathName> descendantsMapping = testItemRepository.selectPathNames(items);

		Set<Long> idsToDelete = Sets.newHashSet(descendantsMapping.keySet());

		descendantsMapping.forEach((key, value) -> value.getItemPaths().forEach(ip -> {
			if (idsToDelete.contains(ip.getId())) {
				idsToDelete.remove(key);
			}
		}));

		List<TestItem> parentsToUpdate = testItemRepository.findAllById(items.stream()
				.filter(it -> idsToDelete.contains(it.getItemId()))
				.map(TestItem::getParentId)
				.filter(Objects::nonNull)
				.collect(toList()));

		Set<Long> removedItems = testItemRepository.findAllById(idsToDelete)
				.stream()
				.map(TestItem::getPath)
				.collect(toList())
				.stream()
				.flatMap(path -> testItemRepository.selectAllDescendantsIds(path).stream())
				.collect(toSet());

		idsToDelete.forEach(itemContentRemover::remove);
		eventPublisher.publishEvent(new ElementsDeletedEvent(
				items,
				projectDetails.getProjectId(),
				elementsCounterService.countNumberOfItemElements(items)
		));
		testItemRepository.deleteAllByItemIdIn(idsToDelete);

		launches.forEach(it -> it.setHasRetries(launchRepository.hasRetries(it.getId())));

		parentsToUpdate.forEach(it -> it.setHasChildren(testItemRepository.hasChildren(it.getItemId(), it.getPath())));

		if (CollectionUtils.isNotEmpty(removedItems)) {
			logIndexer.indexItemsRemoveAsync(projectDetails.getProjectId(), removedItems);
			attachmentRepository.moveForDeletionByItems(removedItems);
		}

		return idsToDelete.stream().map(COMPOSE_DELETE_RESPONSE).collect(toList());
	}

	private static final Function<Long, OperationCompletionRS> COMPOSE_DELETE_RESPONSE = it -> {
		String message = formattedSupplier("Test Item with ID = '{}' has been successfully deleted.", it).get();
		return new OperationCompletionRS(message);
	};

	/**
	 * Validate {@link ReportPortalUser} credentials, {@link TestItemResults#getStatus()},
	 * {@link Launch#getStatus()} and {@link Launch} affiliation to the {@link com.epam.ta.reportportal.entity.project.Project}
	 *
	 * @param testItem       {@link TestItem}
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link ReportPortalUser.ProjectDetails}
	 */
	private void validate(TestItem testItem, Launch launch, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
					formattedSupplier("Deleting testItem '{}' is not under specified project '{}'",
							testItem.getItemId(),
							projectDetails.getProjectId()
					)
			);
		}
		expect(testItem.getRetryOf(), Objects::isNull).verify(ErrorType.RETRIES_HANDLER_ERROR,
				Suppliers.formattedSupplier("Unable to delete test item ['{}'] because it is a retry", testItem.getItemId()).get()
		);
		expect(testItem.getItemResults().getStatus(), not(it -> it.equals(StatusEnum.IN_PROGRESS))).verify(TEST_ITEM_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete test item ['{}'] in progress state", testItem.getItemId())
		);
		expect(launch.getStatus(), not(it -> it.equals(StatusEnum.IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete test item ['{}'] under launch ['{}'] with 'In progress' state",
						testItem.getItemId(),
						launch.getId()
				)
		);
	}
}
