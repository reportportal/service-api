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

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.reportportal.rules.exception.ErrorType.LOG_NOT_FOUND;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.querygen.constant.LogCriteriaConstant.CRITERIA_ITEM_LAUNCH_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_MESSAGE;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATH;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_RETRY_PARENT_LAUNCH_ID;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.CompositeFilterCondition;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.FilterTarget;
import com.epam.ta.reportportal.commons.querygen.LogFilterPreparator;
import com.epam.ta.reportportal.commons.querygen.ProjectFilter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.log.GetLogHandler;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.constant.LogRepositoryConstants;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.NestedItem;
import com.epam.ta.reportportal.entity.item.NestedItemPage;
import com.epam.ta.reportportal.entity.item.NestedStep;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.model.log.GetLogsUnderRq;
import com.epam.ta.reportportal.model.log.LogResource;
import com.epam.ta.reportportal.service.LogTypeResolver;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.LogConverter;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.google.common.collect.Lists;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.jooq.Operator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Implementation of GET log operations
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
@Service
@RequiredArgsConstructor
public class GetLogHandlerImpl implements GetLogHandler {

  public static final String EXCLUDE_PASSED_LOGS = "excludePassedLogs";
  public static final String EXCLUDE_EMPTY_STEPS = "excludeEmptySteps";
  public static final String EXCLUDE_LOG_CONTENT = "excludeLogContent";
  public static final String INCLUDE_SEARCH_FILTER = "includeSearchFilter";

  private static final int NESTED_STEP_MAX_PAGE_SIZE = 300;

  private static final int LOG_UNDER_ITEM_BATCH_SIZE = 5;

  private final LogRepository logRepository;

  private final LogService logService;

  private final TestItemRepository testItemRepository;

  private final TestItemService testItemService;

  private final LogConverter logConverter;

  private final LogFilterPreparator logFilterPreparator;

  private final LogTypeResolver logTypeResolver;


  @Override
  public com.epam.ta.reportportal.model.Page<LogResource> getLogs(@Nullable String path,
      ReportPortalUser.ProjectDetails projectDetails, Filter filterable, Pageable pageable) {
    ofNullable(path).ifPresent(p -> updateFilter(filterable, p));
    Filter resolvedFilter = logFilterPreparator.prepare(filterable, projectDetails.getProjectId());
    Page<LogFull> logFullPage =
        logService.findByFilter(ProjectFilter.of(resolvedFilter, projectDetails.getProjectId()),
            pageable
        );
    List<LogResource> resources = logConverter.toResources(logFullPage.getContent(),
        projectDetails.getProjectId());
    return PagedResourcesAssembler.<LogResource>pageConverter().apply(
        new PageImpl<>(resources, logFullPage.getPageable(), logFullPage.getTotalElements())
    );
  }

  @Override
  public Map<Long, List<LogResource>> getLogs(GetLogsUnderRq logsUnderRq,
      ReportPortalUser.ProjectDetails projectDetails) {

    final int logLevel = logTypeResolver.resolveLogLevelFromName(projectDetails.getProjectId(),
        logsUnderRq.getLogLevel());

    return testItemRepository.findAllById(logsUnderRq.getItemIds()).stream()
        .collect(toMap(TestItem::getItemId, item -> {
          final Launch launch = testItemService.getEffectiveLaunch(item);
          validate(launch, projectDetails);
          List<LogFull> logs = logService.findLatestUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(
              launch.getId(), item.getItemId(), logLevel, LOG_UNDER_ITEM_BATCH_SIZE);
          return logConverter.toResources(logs, projectDetails.getProjectId());
        }));
  }

  @Override
  public long getPageNumber(Long logId, ReportPortalUser.ProjectDetails projectDetails,
      Filter filterable, Pageable pageable) {
    return logRepository.getPageNumber(logId,
        logFilterPreparator.prepare(filterable, projectDetails.getProjectId()),
        pageable);
  }

  @Override
  public LogResource getLog(String logId, ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user) {
    LogFull logFull;
    try {
      logFull = findById(Long.parseLong(logId));
    } catch (NumberFormatException e) {
      logFull = findByUuid(logId);
    }
    validate(logFull, projectDetails);
    return logConverter.toResource(logFull);
  }

  @Override
  public com.epam.ta.reportportal.model.Page<?> getNestedItems(Long parentId,
      ReportPortalUser.ProjectDetails projectDetails,
      Map<String, String> params, Queryable queryable, Pageable pageable) {

    TestItem parentItem = testItemRepository.findById(parentId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, parentId));
    Launch launch = testItemService.getEffectiveLaunch(parentItem);
    validate(launch, projectDetails);

    Queryable resolvedFilter = logFilterPreparator.prepare(queryable,
        projectDetails.getProjectId());

    Boolean excludeEmptySteps =
        ofNullable(params.get(EXCLUDE_EMPTY_STEPS)).map(BooleanUtils::toBoolean).orElse(false);
    Boolean excludePassedLogs =
        ofNullable(params.get(EXCLUDE_PASSED_LOGS)).map(BooleanUtils::toBoolean).orElse(false);

    Page<NestedItem> nestedItems = logRepository.findNestedItems(parentId, excludeEmptySteps,
        isLogsExclusionRequired(parentItem, excludePassedLogs), resolvedFilter, pageable
    );

    List<NestedItem> content = nestedItems.getContent();

    Map<String, List<NestedItem>> result =
        content.stream().collect(groupingBy(NestedItem::getType));

    Map<Long, LogFull> logMap = ofNullable(result.get(LogRepositoryConstants.LOG)).map(
        logs -> logService.findAllById(
                logs.stream().map(NestedItem::getId).collect(Collectors.toSet())).stream()
            .collect(toMap(LogFull::getId, l -> l))).orElseGet(Collections::emptyMap);

    resolvedFilter.getFilterConditions().add(getLaunchCondition(launch.getId()));
    resolvedFilter.getFilterConditions().add(getParentPathCondition(parentItem));
    Map<Long, NestedStep> nestedStepMap = ofNullable(result.get(LogRepositoryConstants.ITEM)).map(
        testItems -> testItemRepository.findAllNestedStepsByIds(
            testItems.stream().map(NestedItem::getId).collect(Collectors.toSet()), resolvedFilter,
            excludePassedLogs
        ).stream().collect(toMap(NestedStep::getId, i -> i))).orElseGet(Collections::emptyMap);

    Map<Long, LogResource> logResourceMap = logConverter.toResources(logMap.values(),
            projectDetails.getProjectId()).stream()
        .collect(toMap(LogResource::getId, Function.identity()));

    List<Object> resources = Lists.newArrayListWithExpectedSize(content.size());
    content.forEach(nestedItem -> {
      if (LogRepositoryConstants.LOG.equals(nestedItem.getType())) {
        ofNullable(logResourceMap.get(nestedItem.getId()))
            .ifPresent(resources::add);
      } else if (LogRepositoryConstants.ITEM.equals(nestedItem.getType())) {
        ofNullable(nestedStepMap.get(nestedItem.getId())).map(
            TestItemConverter.TO_NESTED_STEP_RESOURCE).ifPresent(resources::add);
      }
    });

    return PagedResourcesAssembler.pageConverter().apply(
        PageableExecutionUtils.getPage(resources, nestedItems.getPageable(),
            nestedItems::getTotalElements
        ));
  }

  @Override
  public List<PagedLogResource> getLogsWithLocation(Long parentId,
      ReportPortalUser.ProjectDetails projectDetails, Map<String, String> params,
      Queryable queryable, Pageable pageable) {

    validateTestItemAndLaunch(parentId, projectDetails);

    Queryable resolvedFilter = logFilterPreparator.prepare(queryable,
        projectDetails.getProjectId());

    LogLocationParams locationParams = extractLogLocationParams(params);

    List<PagedLogResource> loadedLogs = locationParams.includeSearchFilter()
        ? loadLogsWithSearchFilter(parentId, locationParams, resolvedFilter, pageable)
        : loadLogsErrorsOnly(parentId, locationParams, resolvedFilter, pageable);

    if (!locationParams.excludeLogContent() && !loadedLogs.isEmpty()) {
      enrichLogsWithContent(loadedLogs, projectDetails.getProjectId());
    }
    return loadedLogs;
  }

  private List<PagedLogResource> loadLogsErrorsOnly(Long parentId, LogLocationParams params,
      Queryable queryable, Pageable pageable) {

    Predicate<NestedItemPage> inclusionFilter = item ->
        item.getType().equals(LogRepositoryConstants.ITEM)
            || item.getLogLevel() >= LogLevel.ERROR_INT;

    return processNestedItemsWithFilter(parentId, params, queryable, pageable, inclusionFilter);
  }


  private List<PagedLogResource> loadLogsWithSearchFilter(Long parentId, LogLocationParams params,
      Queryable queryable, Pageable pageable) {

    Set<Long> matchingLogIds = collectMatchingLogIdsRecursively(parentId, params, queryable,
        pageable.getSort());

    Queryable queryableWithoutMessage = removeMessageFilters(queryable);

    Predicate<NestedItemPage> inclusionFilter = item ->
        item.getType().equals(LogRepositoryConstants.ITEM) || matchingLogIds.contains(item.getId());

    return processNestedItemsWithFilter(parentId, params, queryableWithoutMessage, pageable,
        inclusionFilter);
  }

  private List<PagedLogResource> processNestedItemsWithFilter(Long parentId,
      LogLocationParams params, Queryable queryable, Pageable pageable,
      Predicate<NestedItemPage> inclusionFilter) {

    List<PagedLogResource> results = new ArrayList<>();
    processNestedItems(parentId, results, Collections.emptyList(), params.excludeEmptySteps(),
        params.excludePassedLogs(), queryable, pageable, inclusionFilter);
    return results;
  }

  private void processNestedItems(Long parentId, List<PagedLogResource> results,
      List<Map.Entry<Long, Integer>> pagesLocation, boolean excludeEmptySteps,
      boolean excludePassedLogs, Queryable queryable, Pageable pageable,
      Predicate<NestedItemPage> inclusionFilter) {

    final TestItem parentItem = getValidatedTestItem(parentId);

    if (isLogsExclusionRequired(parentItem, excludePassedLogs)) {
      return;
    }

    logRepository.findNestedItemsWithPage(parentId, excludeEmptySteps,
            isLogsExclusionRequired(parentItem, excludePassedLogs), queryable, pageable)
        .stream()
        .filter(inclusionFilter)
        .forEach(nestedItem -> processNestedItem(nestedItem, results, pagesLocation,
            excludeEmptySteps, excludePassedLogs, queryable, pageable, inclusionFilter));
  }

  private void processNestedItem(NestedItemPage nestedItem, List<PagedLogResource> results,
      List<Map.Entry<Long, Integer>> pagesLocation, boolean excludeEmptySteps,
      boolean excludePassedLogs, Queryable queryable, Pageable pageable,
      Predicate<NestedItemPage> inclusionFilter) {

    List<Map.Entry<Long, Integer>> itemLocation = new ArrayList<>(pagesLocation);
    itemLocation.add(new AbstractMap.SimpleEntry<>(nestedItem.getId(), nestedItem.getPageNumber()));

    if (nestedItem.getType().equals(LogRepositoryConstants.ITEM)) {
      processNestedItems(nestedItem.getId(), results, itemLocation, excludeEmptySteps,
          excludePassedLogs, queryable,
          PageRequest.of(1, NESTED_STEP_MAX_PAGE_SIZE, pageable.getSort()), inclusionFilter);
    } else {
      PagedLogResource pagedLogResource = new PagedLogResource();
      pagedLogResource.setId(nestedItem.getId());
      pagedLogResource.setPagesLocation(itemLocation);
      results.add(pagedLogResource);
    }
  }

  private Set<Long> collectMatchingLogIdsRecursively(Long parentId, LogLocationParams params,
      Queryable queryable, Sort sort) {

    TestItem parentItem = getValidatedTestItem(parentId);

    if (isLogsExclusionRequired(parentItem, params.excludePassedLogs())) {
      return Collections.emptySet();
    }

    Page<NestedItem> items = logRepository.findNestedItems(parentId, params.excludeEmptySteps(),
        isLogsExclusionRequired(parentItem, params.excludePassedLogs()), queryable,
        PageRequest.of(0, NESTED_STEP_MAX_PAGE_SIZE, sort));

    return items.getContent().stream()
        .flatMap(item -> LogRepositoryConstants.ITEM.equals(item.getType())
            ? collectMatchingLogIdsRecursively(item.getId(), params, queryable, sort).stream()
            : Stream.of(item.getId()))
        .collect(Collectors.toSet());
  }

  private Queryable removeMessageFilters(Queryable queryable) {
    var filteredConditions = queryable.getFilterConditions().stream()
        .filter(this::shouldKeepCondition)
        .toList();

    return new Filter(queryable.getTarget().getClazz(), filteredConditions);
  }

  private boolean shouldKeepCondition(ConvertibleCondition condition) {
    return !(condition instanceof FilterCondition filterCondition
        && CRITERIA_LOG_MESSAGE.equalsIgnoreCase(filterCondition.getSearchCriteria()));
  }

  /**
   * Validate log item on existence, availability under specified project, etc.
   *
   * @param log            - logFull item
   * @param projectDetails Project details
   */
  private void validate(LogFull log, ReportPortalUser.ProjectDetails projectDetails) {
    Long launchProjectId = ofNullable(log.getTestItem()).map(
            it -> testItemService.getEffectiveLaunch(it).getProjectId())
        .orElseGet(() -> log.getLaunch().getProjectId());

    expect(launchProjectId, equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
        formattedSupplier("Log '{}' is not under '{}' project", log.getId(),
            projectDetails.getProjectName()
        )
    );
  }

  private void validate(Launch launch, ReportPortalUser.ProjectDetails projectDetails) {
    expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(
        FORBIDDEN_OPERATION,
        formattedSupplier("Launch '{}' is not under '{}' project", launch.getId(),
            projectDetails.getProjectName()
        )
    );
  }

  /**
   * Find logFull item by id
   *
   * @param logId - log ID
   * @return - log item
   */
  private LogFull findById(Long logId) {
    return logService.findById(logId)
        .orElseThrow(() -> new ReportPortalException(LOG_NOT_FOUND, logId));
  }

  /**
   * Find logFull item by uuid
   *
   * @param logId - log UUID
   * @return - log item
   */
  private LogFull findByUuid(String logId) {
    return logService.findByUuid(logId)
        .orElseThrow(() -> new ReportPortalException(LOG_NOT_FOUND, logId));
  }

  private FilterCondition getLaunchCondition(Long launchId) {
    return FilterCondition.builder().eq(CRITERIA_ITEM_LAUNCH_ID, String.valueOf(launchId)).build();
  }

  /**
   * Updates 'filterable' with {@link TestItem#getLaunchId()} condition if
   * {@link TestItem#getRetryOf()} is NULL otherwise updates 'filterable' with 'launchId' of the
   * 'retry' parent
   *
   * @param filterable {@link Filter} with {@link FilterTarget#getClazz()} of {@link Log}
   * @param path       {@link TestItem#getPath()} under which {@link Log} entities should be
   *                   searched
   */
  private void updateFilter(Filter filterable, String path) {
    TestItem testItem = testItemRepository.findByPath(path)
        .orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, path));

    updatePathCondition(testItem, filterable);

    Launch launch = testItemService.getEffectiveLaunch(testItem);

    FilterCondition.ConditionBuilder itemLaunchIdConditionBuilder =
        FilterCondition.builder().eq(CRITERIA_ITEM_LAUNCH_ID, String.valueOf(launch.getId()));

    ConvertibleCondition launchIdCondition = ofNullable(testItem.getRetryOf()).map(
        retryOf -> (ConvertibleCondition) new CompositeFilterCondition(
            Lists.newArrayList(itemLaunchIdConditionBuilder.withOperator(Operator.OR).build(),
                FilterCondition.builder()
                    .eq(CRITERIA_RETRY_PARENT_LAUNCH_ID, String.valueOf(launch.getId()))
                    .withOperator(Operator.OR).build()
            ))).orElseGet(itemLaunchIdConditionBuilder::build);

    filterable.getFilterConditions().add(launchIdCondition);

  }

  /**
   * Updates 'path' condition of the {@link TestItem} whose {@link Log} entities should be searched.
   * Required when there are 'Nested Steps' under the {@link TestItem} that is a 'retry'
   *
   * @param testItem   {@link TestItem} containing logs
   * @param filterable {@link Filter} with {@link FilterTarget#getClazz()} of {@link Log}
   */
  private void updatePathCondition(TestItem testItem, Filter filterable) {
    List<ConvertibleCondition> resultConditions =
        filterable.getFilterConditions().stream().flatMap(c -> c.getAllConditions().stream())
            .filter(c -> BooleanUtils.isFalse(
                CRITERIA_PATH.equals(c.getSearchCriteria()) && Condition.UNDER.equals(
                    c.getCondition()))).collect(Collectors.toList());
    filterable.getFilterConditions().clear();

    FilterCondition parentPathCondition = getParentPathCondition(testItem);
    resultConditions.add(ofNullable(testItem.getRetryOf()).map(
        retryParent -> (ConvertibleCondition) new CompositeFilterCondition(
            Lists.newArrayList(parentPathCondition,
                FilterCondition.builder().withOperator(Operator.OR).withCondition(Condition.UNDER)
                    .withSearchCriteria(CRITERIA_PATH).withValue(String.valueOf(testItem.getPath()))
                    .build()
            ))).orElse(parentPathCondition));

    filterable.getFilterConditions().addAll(resultConditions);
  }

  private FilterCondition getParentPathCondition(TestItem parent) {
    String pathValue = ofNullable(parent.getRetryOf()).flatMap(
            retryParentId -> ofNullable(parent.getParentId()).flatMap(testItemRepository::findById)
                .map(retryParent -> retryParent.getPath() + "." + parent.getItemId()))
        .orElse(parent.getPath());
    return FilterCondition.builder().withCondition(Condition.UNDER)
        .withSearchCriteria(CRITERIA_PATH).withValue(pathValue).build();
  }

  /**
   * Method to determine whether logs of the {@link TestItem} with {@link StatusEnum#PASSED} should
   * be retrieved with nested steps or should be excluded from the select query
   *
   * @param parent            {@link Log#getTestItem()}
   * @param excludePassedLogs if 'true' logs of the passed items should be excluded
   * @return 'true' if logs should be excluded from the select query, else 'false'
   */
  private boolean isLogsExclusionRequired(TestItem parent, boolean excludePassedLogs) {
    if (excludePassedLogs) {
      return Stream.of(StatusEnum.values()).filter(StatusEnum::isPositive)
          .anyMatch(s -> s == parent.getItemResults().getStatus());
    }
    return false;
  }

  private TestItem getValidatedTestItem(Long parentId) {
    return testItemRepository.findById(parentId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, parentId));
  }

  private void validateTestItemAndLaunch(Long parentId,
      ReportPortalUser.ProjectDetails projectDetails) {
    TestItem parentItem = getValidatedTestItem(parentId);
    Launch launch = testItemService.getEffectiveLaunch(parentItem);
    validate(launch, projectDetails);
  }

  private LogLocationParams extractLogLocationParams(Map<String, String> params) {
    return new LogLocationParams(
        ofNullable(params.get(EXCLUDE_EMPTY_STEPS)).map(BooleanUtils::toBoolean).orElse(false),
        ofNullable(params.get(EXCLUDE_PASSED_LOGS)).map(BooleanUtils::toBoolean).orElse(false),
        ofNullable(params.get(EXCLUDE_LOG_CONTENT)).map(BooleanUtils::toBoolean).orElse(false),
        ofNullable(params.get(INCLUDE_SEARCH_FILTER)).map(BooleanUtils::toBoolean).orElse(false)
    );
  }

  private void enrichLogsWithContent(List<PagedLogResource> logs, Long projectId) {
    Map<Long, LogFull> logMap = logService.findAllById(logs.stream()
            .map(PagedLogResource::getId)
            .collect(Collectors.toSet()))
        .stream()
        .collect(toMap(LogFull::getId, l -> l));

    logConverter.fillWithLogContent(logMap, logs, projectId);
  }

  private record LogLocationParams(boolean excludeEmptySteps, boolean excludePassedLogs,
                                   boolean excludeLogContent, boolean includeSearchFilter) {

  }
}
