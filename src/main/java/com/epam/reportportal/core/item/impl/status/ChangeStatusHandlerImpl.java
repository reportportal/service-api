/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.core.item.impl.status;

import static com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum.FAILED;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum.INFO;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum.PASSED;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum.SKIPPED;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum.WARN;
import static com.epam.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.core.events.MessageBus;
import com.epam.reportportal.core.events.activity.item.TestItemStatusChangedEvent;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.dao.IssueEntityRepository;
import com.epam.reportportal.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.infrastructure.persistence.jooq.enums.JStatusEnum;
import com.epam.reportportal.model.activity.TestItemActivityResource;
import com.google.common.collect.Lists;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ChangeStatusHandlerImpl implements ChangeStatusHandler {

  private final TestItemRepository testItemRepository;
  private final IssueEntityRepository issueEntityRepository;
  private final MessageBus messageBus;
  private final LaunchRepository launchRepository;
  private final Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping;

  @Autowired
  public ChangeStatusHandlerImpl(TestItemRepository testItemRepository,
      IssueEntityRepository issueEntityRepository, MessageBus messageBus,
      LaunchRepository launchRepository,
      Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping) {
    this.testItemRepository = testItemRepository;
    this.issueEntityRepository = issueEntityRepository;
    this.messageBus = messageBus;
    this.launchRepository = launchRepository;
    this.statusChangingStrategyMapping = statusChangingStrategyMapping;
  }

  @Override
  public void changeParentStatus(TestItem childItem, MembershipDetails membershipDetails, ReportPortalUser user) {
    Long projectId = membershipDetails.getProjectId();
    ofNullable(childItem.getParentId()).flatMap(testItemRepository::findById).ifPresent(parent -> {
      if (parent.isHasChildren()) {
        ofNullable(parent.getItemResults().getIssue()).map(IssueEntity::getIssueId)
            .ifPresent(issueEntityRepository::deleteById);
      }
      if (isParentStatusUpdateRequired(parent)) {
        StatusEnum resolvedStatus = resolveStatus(parent.getItemId());
        if (parent.getItemResults().getStatus() != resolvedStatus) {
          TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(parent, projectId);
          changeStatus(parent, resolvedStatus, user);
          messageBus.publishActivity(
              new TestItemStatusChangedEvent(before, TO_ACTIVITY_RESOURCE.apply(parent, projectId),
                  user.getUserId(), user.getUsername(), membershipDetails.getOrgId()
              ));
          changeParentStatus(parent, membershipDetails, user);
        }

      }
    });
  }

  private boolean isParentStatusUpdateRequired(TestItem parent) {
    return parent.getItemResults().getStatus() != StatusEnum.IN_PROGRESS
        && parent.getItemResults().getStatus() != PASSED
        && parent.getItemResults().getStatus() != FAILED
        && parent.getItemResults().getStatus() != SKIPPED
        && !testItemRepository.hasItemsInStatusByParent(parent.getItemId(), parent.getPath(),
        StatusEnum.IN_PROGRESS.name());
  }

  private StatusEnum resolveStatus(Long itemId) {
    return
        testItemRepository.hasDescendantsNotInStatus(itemId, StatusEnum.PASSED.name(), INFO.name(),
            WARN.name()
        ) ? FAILED : PASSED;
  }

  private void changeStatus(TestItem parent, StatusEnum resolvedStatus, ReportPortalUser user) {
    if (parent.isHasChildren() || !parent.isHasStats()) {
      parent.getItemResults().setStatus(resolvedStatus);
    } else {
      Optional<StatusChangingStrategy> statusChangingStrategy =
          ofNullable(statusChangingStrategyMapping.get(resolvedStatus));
      if (statusChangingStrategy.isPresent()) {
        statusChangingStrategy.get().changeStatus(parent, resolvedStatus, user, false);
      } else {
        parent.getItemResults().setStatus(resolvedStatus);
      }
    }

  }

  @Override
  public void changeLaunchStatus(Launch launch) {
    if (launch.getStatus() != StatusEnum.IN_PROGRESS) {
      if (!launchRepository.hasItemsInStatuses(launch.getId(),
          Lists.newArrayList(JStatusEnum.IN_PROGRESS)
      )) {
        StatusEnum launchStatus = launchRepository.hasRootItemsWithStatusNotEqual(launch.getId(),
            StatusEnum.PASSED.name(), INFO.name(), WARN.name()
        ) ? FAILED : PASSED;
        launch.setStatus(launchStatus);
      }
    }
  }
}
