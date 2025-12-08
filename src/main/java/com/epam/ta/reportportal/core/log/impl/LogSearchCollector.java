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

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.log.impl.GetLogHandlerImpl.LogLocationParams;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.constant.LogRepositoryConstants;
import com.epam.ta.reportportal.entity.item.NestedItem;
import com.epam.ta.reportportal.entity.item.NestedItemPage;
import com.epam.ta.reportportal.entity.item.TestItem;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Collects logs recursively with page locations for search functionality.
 */
@Component
@RequiredArgsConstructor
public class LogSearchCollector {

  private static final int NESTED_STEP_MAX_PAGE_SIZE = 300;

  private final LogRepository logRepository;
  private final TestItemRepository testItemRepository;

  /**
   * Collects all matching logs with their page locations.
   *
   * @param parentId parent item ID
   * @param params log location parameters
   * @param filterWithMessage filter including message search criteria
   * @param filterNoMessage filter without message criteria (for correct page numbering)
   * @param pageable pagination settings (used for sorting and page size calculation)
   * @return list of all matching logs with page locations
   */
  public List<PagedLogResource> collect(Long parentId, LogLocationParams params,
      Queryable filterWithMessage, Queryable filterNoMessage, Pageable pageable) {

    List<PagedLogResource> allResults = new ArrayList<>();

    var context = new CollectionContext(params, filterWithMessage, filterNoMessage, pageable,
        allResults);

    collectRecursively(parentId, Collections.emptyList(), context);

    return allResults;
  }

  private void collectRecursively(Long parentId, List<Map.Entry<Long, Integer>> pagesLocation,
      CollectionContext context) {

    TestItem parentItem = testItemRepository.findById(parentId).orElse(null);
    if (shouldSkipItem(parentItem, context.params())) {
      return;
    }

    Set<Long> matchingLogIds = findMatchingLogIds(parentId, context);

    List<NestedItemPage> itemsWithPages = fetchItemsWithPageNumbers(parentId, pagesLocation,
        context);

    processItems(itemsWithPages, matchingLogIds, pagesLocation, context);
  }

  private boolean shouldSkipItem(TestItem item, LogLocationParams params) {
    return Objects.isNull(item) || shouldExcludePassedLogs(item, params.excludePassedLogs());
  }

  private Set<Long> findMatchingLogIds(Long parentId, CollectionContext context) {
    boolean excludeLogs = shouldExcludePassedLogsForParent(parentId, context.params());

    Page<NestedItem> matchingItems = logRepository.findNestedItems(parentId,
        context.params().excludeEmptySteps(), excludeLogs, context.filterWithMessage(),
        PageRequest.of(0, NESTED_STEP_MAX_PAGE_SIZE, context.pageable().getSort()));

    return matchingItems.getContent().stream()
        .filter(item -> LogRepositoryConstants.LOG.equals(item.getType()))
        .map(NestedItem::getId)
        .collect(Collectors.toSet());
  }

  private List<NestedItemPage> fetchItemsWithPageNumbers(Long parentId,
      List<Map.Entry<Long, Integer>> pagesLocation, CollectionContext context) {
    boolean excludeLogs = shouldExcludePassedLogsForParent(parentId, context.params());

    int pageSize = pagesLocation.isEmpty()
        ? context.pageable().getPageSize()
        : NESTED_STEP_MAX_PAGE_SIZE;
    Pageable locationPageable = PageRequest.of(0, pageSize, context.pageable().getSort());

    return logRepository.findNestedItemsWithPage(parentId, context.params().excludeEmptySteps(),
        excludeLogs, context.filterNoMessage(), locationPageable);
  }

  private void processItems(List<NestedItemPage> items, Set<Long> matchingLogIds,
      List<Map.Entry<Long, Integer>> pagesLocation, CollectionContext context) {

    items.forEach(item -> processItem(item, matchingLogIds, pagesLocation, context));
  }

  private void processItem(NestedItemPage item, Set<Long> matchingLogIds,
      List<Map.Entry<Long, Integer>> pagesLocation,
      CollectionContext context) {

    List<Map.Entry<Long, Integer>> updatedLocation = buildPageLocation(item, pagesLocation);

    switch (item.getType()) {
      case LogRepositoryConstants.ITEM ->
          collectRecursively(item.getId(), updatedLocation, context);
      case LogRepositoryConstants.LOG -> {
        if (matchingLogIds.contains(item.getId())) {
          addLogResult(item, updatedLocation, context);
        }
      }
      default -> { /* ignore unknown types */ }
    }
  }

  private List<Map.Entry<Long, Integer>> buildPageLocation(NestedItemPage item,
      List<Map.Entry<Long, Integer>> parentLocation) {
    List<Map.Entry<Long, Integer>> location = new ArrayList<>(parentLocation);
    location.add(new AbstractMap.SimpleEntry<>(item.getId(), item.getPageNumber()));
    return location;
  }

  private void addLogResult(NestedItemPage item, List<Map.Entry<Long, Integer>> location,
      CollectionContext context) {
    PagedLogResource resource = new PagedLogResource();
    resource.setId(item.getId());
    resource.setPagesLocation(location);
    context.results().add(resource);
  }

  private boolean shouldExcludePassedLogsForParent(Long parentId, LogLocationParams params) {
    if (!params.excludePassedLogs()) {
      return false;
    }
    TestItem item = testItemRepository.findById(parentId).orElse(null);
    return Objects.nonNull(item) && shouldExcludePassedLogs(item, true);
  }

  private boolean shouldExcludePassedLogs(TestItem item, boolean excludePassedLogs) {
    if (!excludePassedLogs) {
      return false;
    }
    return item.getItemResults().getStatus().isPositive();
  }

  private record CollectionContext(LogLocationParams params, Queryable filterWithMessage,
                                   Queryable filterNoMessage, Pageable pageable,
                                   List<PagedLogResource> results) {

  }
}
