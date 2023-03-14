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
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.ACCESS_DENIED;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.CHILD_START_TIME_EARLIER_THAN_PARENT;
import static com.epam.ta.reportportal.ws.model.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.TEST_ITEM_NOT_FOUND;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.item.identity.IdentityUtil;
import com.epam.ta.reportportal.core.item.identity.TestCaseHashGenerator;
import com.epam.ta.reportportal.core.item.identity.UniqueIdGenerator;
import com.epam.ta.reportportal.core.item.impl.retry.RetryHandler;
import com.epam.ta.reportportal.core.item.impl.retry.RetrySearcher;
import com.epam.ta.reportportal.core.item.validator.parent.ParentItemValidator;
import com.epam.ta.reportportal.core.launch.rerun.RerunHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.TestItemBuilder;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Start Test Item operation default implementation
 *
 * @author Andrei Varabyeu
 * @author Pavel Bortnik
 */
@Service
@Primary
@Transactional
class StartTestItemHandlerImpl implements StartTestItemHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(StartTestItemHandlerImpl.class);

  private final TestItemRepository testItemRepository;

  private final LaunchRepository launchRepository;

  private final UniqueIdGenerator uniqueIdGenerator;

  private final TestCaseHashGenerator testCaseHashGenerator;

  private final RerunHandler rerunHandler;

  private final List<ParentItemValidator> parentItemValidators;

  private final RetrySearcher retrySearcher;
  private final RetryHandler retryHandler;

  @Autowired
  public StartTestItemHandlerImpl(TestItemRepository testItemRepository,
      LaunchRepository launchRepository,
      UniqueIdGenerator uniqueIdGenerator, TestCaseHashGenerator testCaseHashGenerator,
      RerunHandler rerunHandler,
      List<ParentItemValidator> parentItemValidators,
      @Qualifier("uniqueIdRetrySearcher") RetrySearcher retrySearcher,
      RetryHandler retryHandler) {
    this.testItemRepository = testItemRepository;
    this.launchRepository = launchRepository;
    this.uniqueIdGenerator = uniqueIdGenerator;
    this.testCaseHashGenerator = testCaseHashGenerator;
    this.rerunHandler = rerunHandler;
    this.parentItemValidators = parentItemValidators;
    this.retrySearcher = retrySearcher;
    this.retryHandler = retryHandler;
  }

  @Override
  public ItemCreatedRS startRootItem(ReportPortalUser user,
      ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq) {
    Launch launch = launchRepository.findByUuid(rq.getLaunchUuid())
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, rq.getLaunchUuid()));
    validate(user, projectDetails, rq, launch);

    if (launch.isRerun()) {
      Optional<ItemCreatedRS> rerunCreatedRs = rerunHandler.handleRootItem(rq, launch);
      if (rerunCreatedRs.isPresent()) {
        return rerunCreatedRs.get();
      }
    }

    TestItem item = new TestItemBuilder().addStartItemRequest(rq).addAttributes(rq.getAttributes())
        .addLaunchId(launch.getId()).get();
    testItemRepository.save(item);
    generateUniqueId(launch, item, String.valueOf(item.getItemId()));

    LOGGER.debug("Created new root TestItem {}", item.getUuid());
    return new ItemCreatedRS(item.getUuid(), item.getUniqueId());
  }

  @Override
  public ItemCreatedRS startChildItem(ReportPortalUser user,
      ReportPortalUser.ProjectDetails projectDetails, StartTestItemRQ rq,
      String parentId) {
    boolean isRetry =
        BooleanUtils.toBoolean(rq.isRetry()) || StringUtils.isNotBlank(rq.getRetryOf());

    Launch launch = launchRepository.findByUuid(rq.getLaunchUuid())
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, rq.getLaunchUuid()));

    if (launch.isRerun()) {
      Optional<ItemCreatedRS> rerunCreatedRs = rerunHandler.handleChildItem(rq, launch, parentId);
      if (rerunCreatedRs.isPresent()) {
        return rerunCreatedRs.get();
      }
    }

    final TestItem parentItem;
    if (isRetry) {
      // Lock for test
      Long lockedParentId = testItemRepository.findIdByUuidForUpdate(parentId)
          .orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, parentId));
      parentItem = testItemRepository.getOne(lockedParentId);
    } else {
      parentItem = testItemRepository.findByUuid(parentId)
          .orElseThrow(() -> new ReportPortalException(TEST_ITEM_NOT_FOUND, parentId));
    }

    parentItemValidators.forEach(v -> v.validate(rq, parentItem));

    TestItem item = new TestItemBuilder().addStartItemRequest(rq).addAttributes(rq.getAttributes())
        .addLaunchId(launch.getId()).get();

    if (isRetry) {
      ofNullable(rq.getRetryOf()).flatMap(testItemRepository::findIdByUuidForUpdate)
          .ifPresentOrElse(retryParentId -> {
            saveChildItem(launch, item, parentItem);
            retryHandler.handleRetries(launch, item, retryParentId);
          }, () -> retrySearcher.findPreviousRetry(launch, item, parentItem)
              .ifPresentOrElse(previousRetryId -> {
                saveChildItem(launch, item, parentItem);
                retryHandler.handleRetries(launch, item, previousRetryId);
              }, () -> saveChildItem(launch, item, parentItem)));
    } else {
      saveChildItem(launch, item, parentItem);
    }

    LOGGER.debug("Created new child TestItem {} with root {}", item.getUuid(), parentId);

    if (rq.isHasStats() && !parentItem.isHasChildren()) {
      parentItem.setHasChildren(true);
    }

    return new ItemCreatedRS(item.getUuid(), item.getUniqueId());
  }

  private TestItem saveChildItem(Launch launch, TestItem childItem, TestItem parentItem) {
    childItem.setParentId(parentItem.getItemId());
    testItemRepository.save(childItem);
    generateUniqueId(launch, childItem, parentItem.getPath() + "." + childItem.getItemId());
    return childItem;
  }

  /**
   * Generates and sets {@link TestItem#getUniqueId()} and {@link TestItem#getTestCaseId()} if they
   * are empty
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
      item.setTestCaseHash(testCaseHashGenerator.generate(item, IdentityUtil.getParentIds(item),
          launch.getProjectId()));
    }
  }

  /**
   * Validate {@link ReportPortalUser} credentials, {@link Launch#getStatus()} and {@link Launch}
   * affiliation to the {@link Project}
   *
   * @param user           {@link ReportPortalUser}
   * @param projectDetails {@link ReportPortalUser.ProjectDetails}
   * @param rq             {@link StartTestItemRQ}
   * @param launch         {@link Launch}
   */
  private void validate(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails,
      StartTestItemRQ rq, Launch launch) {
    if (!UserRole.ADMINISTRATOR.equals(user.getUserRole())) {
      expect(projectDetails.getProjectId(), equalTo(launch.getProjectId())).verify(ACCESS_DENIED);
    }
    expect(rq.getStartTime(), Preconditions.sameTimeOrLater(launch.getStartTime())).verify(
        CHILD_START_TIME_EARLIER_THAN_PARENT,
        rq.getStartTime(),
        launch.getStartTime(),
        launch.getId()
    );
    expect(isTrue(BooleanUtils.toBoolean(rq.isRetry())), equalTo(false)).verify(BAD_REQUEST_ERROR,
        "Root test item can't be a retry.");
  }

}
