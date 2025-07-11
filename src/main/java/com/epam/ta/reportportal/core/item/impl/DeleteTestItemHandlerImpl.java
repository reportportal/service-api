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

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.rules.exception.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.reportportal.rules.exception.ErrorType.LAUNCH_IS_NOT_FINISHED;
import static com.epam.reportportal.rules.exception.ErrorType.TEST_ITEM_IS_NOT_FINISHED;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.epam.reportportal.events.ElementsDeletedEvent;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.ElementsCounterService;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.item.DeleteTestItemHandler;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.PathName;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

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

  private final LogService logService;

  @Autowired
  public DeleteTestItemHandlerImpl(TestItemRepository testItemRepository,
      ContentRemover<Long> itemContentRemover, LogIndexer logIndexer,
      LaunchRepository launchRepository, AttachmentRepository attachmentRepository,
      ApplicationEventPublisher eventPublisher,
      ElementsCounterService elementsCounterService, LogService logService) {
    this.testItemRepository = testItemRepository;
    this.itemContentRemover = itemContentRemover;
    this.logIndexer = logIndexer;
    this.launchRepository = launchRepository;
    this.attachmentRepository = attachmentRepository;
    this.eventPublisher = eventPublisher;
    this.elementsCounterService = elementsCounterService;
    this.logService = logService;
  }

  @Override
  public OperationCompletionRS deleteTestItem(Long itemId,
      MembershipDetails membershipDetails, ReportPortalUser user) {
    TestItem item = testItemRepository.findById(itemId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemId));
    Launch launch = launchRepository.findById(item.getLaunchId())
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, item.getLaunchId()));

    validate(item, launch, user, membershipDetails);
    Optional<Long> parentId = ofNullable(item.getParentId());

    Set<Long> itemsForRemove = Sets.newHashSet(
        testItemRepository.selectAllDescendantsIds(item.getPath()));
    itemsForRemove.forEach(itemContentRemover::remove);

    eventPublisher.publishEvent(new ElementsDeletedEvent(item,
        membershipDetails.getProjectId(),
        elementsCounterService.countNumberOfItemElements(item)
    ));
    logService.deleteLogMessageByTestItemSet(membershipDetails.getProjectId(), itemsForRemove);
    itemContentRemover.remove(item.getItemId());
    testItemRepository.deleteById(item.getItemId());

    launch.setHasRetries(launchRepository.hasRetries(launch.getId()));
    parentId.flatMap(testItemRepository::findById)
        .ifPresent(
            p -> p.setHasChildren(testItemRepository.hasChildren(p.getItemId(), p.getPath())));

    logIndexer.indexItemsRemoveAsync(membershipDetails.getProjectId(), itemsForRemove);
    attachmentRepository.moveForDeletionByItems(itemsForRemove);

    return COMPOSE_DELETE_RESPONSE.apply(item.getItemId());
  }

  @Override
  public List<OperationCompletionRS> deleteTestItems(Collection<Long> ids,
      MembershipDetails membershipDetails,
      ReportPortalUser user) {
    List<TestItem> items = testItemRepository.findAllById(ids);

    List<Launch> launches = launchRepository.findAllById(items.stream()
        .map(TestItem::getLaunchId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet()));
    Map<Long, List<TestItem>> launchItemMap = items.stream()
        .collect(Collectors.groupingBy(TestItem::getLaunchId));
    launches.forEach(launch -> launchItemMap.get(launch.getId())
        .forEach(item -> validate(item, launch, user, membershipDetails)));

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
        membershipDetails.getProjectId(),
        elementsCounterService.countNumberOfItemElements(items)
    ));
    logService.deleteLogMessageByTestItemSet(membershipDetails.getProjectId(), removedItems);
    testItemRepository.deleteAllByItemIdIn(idsToDelete);

    launches.forEach(it -> it.setHasRetries(launchRepository.hasRetries(it.getId())));

    parentsToUpdate.forEach(
        it -> it.setHasChildren(testItemRepository.hasChildren(it.getItemId(), it.getPath())));

    if (CollectionUtils.isNotEmpty(removedItems)) {
      logIndexer.indexItemsRemoveAsync(membershipDetails.getProjectId(), removedItems);
      attachmentRepository.moveForDeletionByItems(removedItems);
    }

    return idsToDelete.stream().map(COMPOSE_DELETE_RESPONSE).collect(toList());
  }

  private static final Function<Long, OperationCompletionRS> COMPOSE_DELETE_RESPONSE = it -> {
    String message = formattedSupplier("Test Item with ID = '{}' has been successfully deleted.",
        it).get();
    return new OperationCompletionRS(message);
  };

  /**
   * Validate {@link ReportPortalUser} credentials, {@link TestItemResults#getStatus()},
   * {@link Launch#getStatus()} and {@link Launch} affiliation to the
   * {@link com.epam.ta.reportportal.entity.project.Project}
   *
   * @param testItem       {@link TestItem}
   * @param user           {@link ReportPortalUser}
   * @param membershipDetails {@link MembershipDetails}
   */
  private void validate(TestItem testItem, Launch launch, ReportPortalUser user,
      MembershipDetails membershipDetails) {
    if (user.getUserRole() != UserRole.ADMINISTRATOR) {
      expect(launch.getProjectId(), equalTo(membershipDetails.getProjectId())).verify(
          FORBIDDEN_OPERATION,
          formattedSupplier("Deleting testItem '{}' is not under specified project '{}'",
              testItem.getItemId(),
              membershipDetails.getProjectId()
          )
      );
      if (membershipDetails.getOrgRole().lowerThan(OrganizationRole.MANAGER) && membershipDetails.getProjectRole().equals(ProjectRole.VIEWER)) {
        expect(user.getUserId(), Predicate.isEqual(launch.getUserId()))
            .verify(ACCESS_DENIED, "You are not a launch owner.");
      }
    }
    expect(testItem.getRetryOf(), Objects::isNull).verify(ErrorType.RETRIES_HANDLER_ERROR,
        Suppliers.formattedSupplier("Unable to delete test item ['{}'] because it is a retry",
            testItem.getItemId()).get()
    );
    expect(testItem.getItemResults().getStatus(),
        not(it -> it.equals(StatusEnum.IN_PROGRESS))).verify(TEST_ITEM_IS_NOT_FINISHED,
        formattedSupplier("Unable to delete test item ['{}'] in progress state",
            testItem.getItemId())
    );
    expect(launch.getStatus(), not(it -> it.equals(StatusEnum.IN_PROGRESS))).verify(
        LAUNCH_IS_NOT_FINISHED,
        formattedSupplier(
            "Unable to delete test item ['{}'] under launch ['{}'] with 'In progress' state",
            testItem.getItemId(),
            launch.getId()
        )
    );
  }
}
