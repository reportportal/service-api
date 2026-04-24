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

package com.epam.reportportal.base.core.hierarchy.impl;

import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.FAILED;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.hierarchy.AbstractFinishHierarchyHandler;
import com.epam.reportportal.base.core.item.impl.IssueTypeHandler;
import com.epam.reportportal.base.core.item.impl.retry.RetryHandler;
import com.epam.reportportal.base.core.item.impl.status.ChangeStatusHandler;
import com.epam.reportportal.base.infrastructure.persistence.dao.IssueEntityRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ItemAttributeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Handles finishing a hierarchy of test items by propagating finish requests up the hierarchy.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("finishTestItemHierarchyHandler")
public class FinishTestItemHierarchyHandler extends AbstractFinishHierarchyHandler<TestItem> {

  public FinishTestItemHierarchyHandler(LaunchRepository launchRepository,
      TestItemRepository testItemRepository,
      ItemAttributeRepository itemAttributeRepository, IssueEntityRepository issueEntityRepository,
      RetryHandler retryHandler,
      IssueTypeHandler issueTypeHandler, ChangeStatusHandler changeStatusHandler) {
    super(launchRepository,
        testItemRepository,
        itemAttributeRepository,
        issueEntityRepository,
        retryHandler,
        issueTypeHandler,
        changeStatusHandler
    );
  }

  @Override
  protected boolean isIssueRequired(StatusEnum status, TestItem testItem) {
    return FAILED.equals(status) || ofNullable(testItem.getLaunchId()).map(
            launchId -> evaluateSkippedAttributeValue(status, launchId))
        .orElse(false);
  }

  @Override
  protected Function<Pageable, List<Long>> getItemIdsFunction(boolean hasChildren,
      TestItem testItem, StatusEnum status) {
    return hasChildren ?
        pageable -> testItemRepository.findIdsByHasChildrenAndParentPathAndStatusOrderedByPathLevel(
            testItem.getPath(),
            StatusEnum.IN_PROGRESS,
            pageable.getPageSize(),
            pageable.getOffset()
        ) :
        pageable -> testItemRepository.findIdsByNotHasChildrenAndParentPathAndStatus(
            testItem.getPath(),
            status,
            pageable.getPageSize(),
            pageable.getOffset()
        );
  }

}
