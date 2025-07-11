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

package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.commons.querygen.Condition.CNT;
import static com.epam.ta.reportportal.commons.querygen.Condition.EQ;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.CRITERIA_ITEM_ATTRIBUTE_KEY;
import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.CRITERIA_ITEM_ATTRIBUTE_VALUE;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PARENT_ID;
import static com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver.DEFAULT_FILTER_PREFIX;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.CompositeFilter;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.SuggestInfo;
import com.epam.ta.reportportal.core.analyzer.auto.impl.SuggestItemService;
import com.epam.ta.reportportal.core.analyzer.auto.impl.SuggestedItem;
import com.epam.ta.reportportal.core.item.DeleteTestItemHandler;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.GetTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.item.UpdateTestItemHandler;
import com.epam.ta.reportportal.core.item.history.TestItemsHistoryHandler;
import com.epam.ta.reportportal.core.item.impl.TestCaseSearchHandler;
import com.epam.ta.reportportal.core.item.impl.history.param.HistoryRequestParams;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.model.TestItemHistoryElement;
import com.epam.ta.reportportal.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.model.item.UnlinkExternalIssueRQ;
import com.epam.ta.reportportal.model.item.UpdateTestItemRQ;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.reporting.BulkInfoUpdateRQ;
import com.epam.ta.reportportal.ws.reporting.EntryCreatedAsyncRS;
import com.epam.ta.reportportal.ws.reporting.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.reporting.Issue;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.epam.ta.reportportal.ws.reporting.StartTestItemRQ;
import com.epam.ta.reportportal.ws.reporting.StatisticsResource;
import com.epam.ta.reportportal.ws.reporting.TestItemResource;
import com.epam.ta.reportportal.ws.reporting.TestItemResourceOld;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Operator;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementation for {@link com.epam.ta.reportportal.entity.item.TestItem} entity
 */
@RestController
@RequestMapping("/v1/{projectKey}/item")
@Tag(name = "Test Item", description = "Test Items API collection")
@RequiredArgsConstructor
public class TestItemController {

  public static final String HISTORY_TYPE_PARAM = "type";
  public static final String FILTER_ID_REQUEST_PARAM = "filterId";
  public static final String IS_LATEST_LAUNCHES_REQUEST_PARAM = "isLatest";
  public static final String LAUNCHES_LIMIT_REQUEST_PARAM = "launchesLimit";
  private static final String HISTORY_DEPTH_PARAM = "historyDepth";
  private static final String HISTORY_DEPTH_DEFAULT_VALUE = "5";
  private static final String LAUNCHES_LIMIT_DEFAULT_VALUE = "0";

  private final ProjectExtractor projectExtractor;
  private final StartTestItemHandler startTestItemHandler;
  private final DeleteTestItemHandler deleteTestItemHandler;
  private final FinishTestItemHandler finishTestItemHandler;
  private final UpdateTestItemHandler updateTestItemHandler;
  private final GetTestItemHandler getTestItemHandler;
  private final TestItemsHistoryHandler testItemsHistoryHandler;
  private final SuggestItemService suggestItemService;
  private final TestCaseSearchHandler testCaseSearchHandler;

  /* Report client API */

  @PostMapping(value = {"", "/"})
  @ResponseStatus(CREATED)
  @Operation(summary = "Start a root test item")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public EntryCreatedAsyncRS startRootItem(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestBody @Validated StartTestItemRQ startTestItemRQ) {
    return startTestItemHandler.startRootItem(user,
        projectExtractor.extractMembershipDetails(user, projectKey), startTestItemRQ
    );
  }

  @PostMapping(value = {"/{parentItem}", "/{parentItem}/"})
  @ResponseStatus(CREATED)
  @Operation(summary = "Start a child test item")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public EntryCreatedAsyncRS startChildItem(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @PathVariable String parentItem,
      @RequestBody @Validated StartTestItemRQ startTestItemRQ) {
    return startTestItemHandler.startChildItem(user,
        projectExtractor.extractMembershipDetails(user, projectKey), startTestItemRQ, parentItem
    );
  }

  @PutMapping(value = {"/{testItemId}", "/{testItemId}/"})
  @ResponseStatus(OK)
  @Operation(summary = "Finish test item")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public OperationCompletionRS finishTestItem(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @PathVariable String testItemId,
      @RequestBody @Validated FinishTestItemRQ finishExecutionRQ) {
    return finishTestItemHandler.finishTestItem(user,
        projectExtractor.extractMembershipDetails(user, projectKey), testItemId, finishExecutionRQ
    );
  }

  @Transactional(readOnly = true)
  @GetMapping("/{itemId}")
  @ResponseStatus(OK)
  @Operation(summary = "Find test item by ID")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public TestItemResource getTestItem(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @PathVariable String itemId) {
    return getTestItemHandler.getTestItem(itemId,
        projectExtractor.extractMembershipDetails(user, projectKey), user
    );

  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/uuid/{itemId}", produces = "application/x.reportportal.test.v2+json")
  @ResponseStatus(OK)
  @Operation(summary = "Find test item by UUID")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public TestItemResource getTestItemByUuid(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @PathVariable String itemId) {
    return getTestItemHandler.getTestItem(itemId,
        projectExtractor.extractMembershipDetails(user, projectKey), user
    );
  }

  @Transactional(readOnly = true)
  @GetMapping("/uuid/{itemId}")
  @ResponseStatus(OK)
  @Operation(summary = "Find test item by UUID")
  @ApiResponse(
      responseCode = "200",
      description = "Successful response with dates in timestamp format. "
          + "Response with dates in ISO-8601 format if the custom header "
          + "'Accept: application/x.reportportal.test.v2+json' is used.",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestItemResourceOld.class))
  )
  public TestItemResourceOld getTestItemByUuidTimestamp(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @PathVariable String itemId) {
    var testItem = getTestItemHandler.getTestItem(itemId,
        projectExtractor.extractMembershipDetails(user, projectKey), user
    );
    return TestItemConverter.TO_RESOURCE_TIMESTAMP.apply(testItem);
  }

  @Transactional(readOnly = true)
  @GetMapping("/suggest/{itemId}")
  @ResponseStatus(OK)
  @Operation(summary = "Search suggested items in analyzer for provided one")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public List<SuggestedItem> getSuggestedItems(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @PathVariable Long itemId) {
    return suggestItemService.suggestItems(itemId,
        projectExtractor.extractMembershipDetails(user, projectKey), user
    );
  }

  @GetMapping("/suggest/cluster/{clusterId}")
  @ResponseStatus(OK)
  @Operation(summary = "Search suggested items in analyzer for provided one")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public List<SuggestedItem> getSuggestedClusterItems(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @PathVariable Long clusterId) {
    return suggestItemService.suggestClusterItems(clusterId,
        projectExtractor.extractMembershipDetails(user, projectKey), user
    );
  }

  @Transactional
  @PutMapping("/suggest/choice")
  @ResponseStatus(OK)
  @Operation(summary = "Handle user choice from suggested items")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public OperationCompletionRS handleSuggestChoose(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestBody @Validated List<SuggestInfo> request) {
    projectExtractor.extractMembershipDetails(user, projectKey);
    return suggestItemService.handleSuggestChoice(request);
  }

  //TODO check pre-defined filter
  @Transactional(readOnly = true)
  @GetMapping
  @ResponseStatus(OK)
  @Operation(summary = "Find test items by specified filter")
  @Schema(implementation = TestItemHistoryElement.class)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public Page<TestItemResource> getTestItems(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @Nullable
  @Parameter(hidden = true) @RequestParam(value = DEFAULT_FILTER_PREFIX + EQ
      + CRITERIA_LAUNCH_ID, required = false)
  Long launchId,
      @Nullable @RequestParam(value = FILTER_ID_REQUEST_PARAM, required = false) Long filterId,
      @RequestParam(value = IS_LATEST_LAUNCHES_REQUEST_PARAM, defaultValue = "false", required = false)
      boolean isLatest,
      @RequestParam(value = LAUNCHES_LIMIT_REQUEST_PARAM, defaultValue = "0", required = false)
      int launchesLimit, @FilterFor(TestItem.class) Filter filter,
      @FilterFor(TestItem.class) Queryable predefinedFilter,
      @SortFor(TestItem.class) Pageable pageable) {
    return getTestItemHandler.getTestItems(
        new CompositeFilter(Operator.AND, filter, predefinedFilter), pageable,
        projectExtractor.extractMembershipDetails(user, projectKey), user, launchId, filterId,
        isLatest, launchesLimit
    );
  }

  @Transactional(readOnly = true)
  @GetMapping("/v2")
  @ResponseStatus(OK)
  @Operation(summary = "Find test items by specified filter")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public Page<TestItemResource> getTestItemsV2(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @RequestParam Map<String, String> params,
      @FilterFor(TestItem.class) Filter filter,
      @FilterFor(TestItem.class) Queryable predefinedFilter,
      @SortFor(TestItem.class) Pageable pageable) {
    // tmp return null for project, to fix perf issue
    if ("libg-140".equalsIgnoreCase(projectKey)) {
      return null;
    }
    return getTestItemHandler.getTestItemsByProvider(
        new CompositeFilter(Operator.AND, filter, predefinedFilter), pageable,
        projectExtractor.extractMembershipDetails(user, projectKey), user, params
    );
  }

  @Transactional(readOnly = true)
  @GetMapping("/statistics")
  @ResponseStatus(OK)
  @Operation(summary = "Find accumulated statistics of items by specified filter")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public StatisticsResource getTestItems(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @FilterFor(TestItem.class) Filter filter,
      @FilterFor(TestItem.class) Queryable predefinedFilter,
      @RequestParam Map<String, String> params) {
    return getTestItemHandler.getStatisticsByProvider(
        new CompositeFilter(Operator.AND, filter, predefinedFilter),
        projectExtractor.extractMembershipDetails(user, projectKey), user, params
    );
  }

  @Transactional
  @DeleteMapping("/{itemId}")
  @ResponseStatus(OK)
  @Operation(summary = "Delete test item")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public OperationCompletionRS deleteTestItem(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @PathVariable Long itemId) {
    return deleteTestItemHandler.deleteTestItem(itemId,
        projectExtractor.extractMembershipDetails(user, projectKey), user
    );
  }

  @Transactional
  @DeleteMapping
  @ResponseStatus(OK)
  @Operation(summary = "Delete test items by specified ids")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public List<OperationCompletionRS> deleteTestItems(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @RequestParam(value = "ids") Set<Long> ids) {
    return deleteTestItemHandler.deleteTestItems(ids,
        projectExtractor.extractMembershipDetails(user, projectKey), user
    );
  }

  @Transactional
  @PutMapping
  @ResponseStatus(OK)
  @Operation(summary = "Update issues of specified test items")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public List<Issue> defineTestItemIssueType(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestBody @Validated DefineIssueRQ request) {
    return updateTestItemHandler.defineTestItemsIssues(
        projectExtractor.extractMembershipDetails(user, projectKey), request, user);
  }

  @Transactional(readOnly = true)
  @GetMapping("/history")
  @ResponseStatus(OK)
  @Operation(summary = "Load history of test items")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public Page<TestItemHistoryElement> getItemsHistory(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @FilterFor(TestItem.class) Filter filter,
      @FilterFor(TestItem.class) Queryable predefinedFilter,
      @SortFor(TestItem.class) Pageable pageable, @Nullable
  @Parameter(hidden = true) @RequestParam(value = DEFAULT_FILTER_PREFIX + EQ
      + CRITERIA_PARENT_ID, required = false)
  Long parentId, @Nullable
  @Parameter(hidden = true) @RequestParam(value = DEFAULT_FILTER_PREFIX + EQ
      + CRITERIA_ID, required = false)
  Long itemId, @Nullable
  @Parameter(hidden = true) @RequestParam(value = DEFAULT_FILTER_PREFIX + EQ
      + CRITERIA_LAUNCH_ID, required = false)
  Long launchId,
      @Nullable @RequestParam(value = HISTORY_TYPE_PARAM, required = false) String type,
      @Nullable @RequestParam(value = FILTER_ID_REQUEST_PARAM, required = false) Long filterId,
      @RequestParam(value = IS_LATEST_LAUNCHES_REQUEST_PARAM, defaultValue = "false", required = false)
      boolean isLatest,
      @RequestParam(value = LAUNCHES_LIMIT_REQUEST_PARAM, defaultValue = "0", required = false)
      int launchesLimit,
      @RequestParam(value = HISTORY_DEPTH_PARAM, required = false, defaultValue = HISTORY_DEPTH_DEFAULT_VALUE)
      int historyDepth) {

    return testItemsHistoryHandler.getItemsHistory(
        projectExtractor.extractMembershipDetails(user, projectKey),
        new CompositeFilter(Operator.AND, filter, predefinedFilter), pageable,
        HistoryRequestParams.of(historyDepth, parentId, itemId, launchId, type, filterId,
            launchesLimit, isLatest
        ), user
    );
  }

  @Transactional(readOnly = true)
  @GetMapping("/ticket/ids")
  @ResponseStatus(OK)
  @Operation(summary = "Get tickets that contains a term as a part inside for specified launch")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public List<String> getTicketIds(@AuthenticationPrincipal ReportPortalUser user,
      @PathVariable String projectKey, @RequestParam(value = "launch") Long id,
      @RequestParam(value = "term") String term) {
    return getTestItemHandler.getTicketIds(id, normalizeId(term));
  }

  @Transactional(readOnly = true)
  @GetMapping("/ticket/ids/all")
  @ResponseStatus(OK)
  @Operation(summary = "Get tickets that contains a term as a part inside for specified launch")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public List<String> getTicketIdsForProject(@AuthenticationPrincipal ReportPortalUser user,
      @PathVariable String projectKey, @RequestParam(value = "term") String term) {
    return getTestItemHandler.getTicketIds(
        projectExtractor.extractMembershipDetails(user, projectKey), normalizeId(term));
  }

  @Transactional(readOnly = true)
  @GetMapping("/attribute/keys")
  @ResponseStatus(OK)
  @Operation(summary = "Get all unique attribute keys on the project")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public List<String> getUniqueAttributeKeys(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestParam(value = "launch", required = false) Long launchId,
      @RequestParam(value = DEFAULT_FILTER_PREFIX + CNT
          + CRITERIA_ITEM_ATTRIBUTE_KEY) String keyPart) {
    return getTestItemHandler.getUniqueAttributeKeys(
        projectExtractor.extractMembershipDetails(user, projectKey), keyPart, launchId);
  }

  @Transactional(readOnly = true)
  @GetMapping("/attribute/values")
  @ResponseStatus(OK)
  @Operation(summary = "Get all unique attribute values on the project")
  public List<String> getAttributeValues(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestParam(value = "launch", required = false) Long launchId,
      @RequestParam(value = DEFAULT_FILTER_PREFIX + EQ
          + CRITERIA_ITEM_ATTRIBUTE_KEY, required = false) String key,
      @RequestParam(value = DEFAULT_FILTER_PREFIX + CNT + CRITERIA_ITEM_ATTRIBUTE_VALUE)
      String valuePart) {
    return getTestItemHandler.getUniqueAttributeValues(
        projectExtractor.extractMembershipDetails(user, projectKey), key, valuePart, launchId);
  }

  @Transactional(readOnly = true)
  @GetMapping("/attribute/keys/all")
  @ResponseStatus(OK)
  @Operation(summary = "Get all unique attribute keys of specified launch")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public List<String> getAttributeKeysForProject(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestParam(value = DEFAULT_FILTER_PREFIX + CNT + CRITERIA_ITEM_ATTRIBUTE_KEY)
      String value, @RequestParam(value = FILTER_ID_REQUEST_PARAM) Long launchFilterId,
      @RequestParam(value = IS_LATEST_LAUNCHES_REQUEST_PARAM, defaultValue = "false", required = false)
      boolean isLatest,
      @RequestParam(value = LAUNCHES_LIMIT_REQUEST_PARAM, defaultValue = "0") int launchesLimit) {
    return getTestItemHandler.getAttributeKeys(launchFilterId, isLatest, launchesLimit,
        projectExtractor.extractMembershipDetails(user, projectKey), value
    );
  }

  @Transactional(readOnly = true)
  @GetMapping("/step/attribute/keys")
  @ResponseStatus(OK)
  @Operation(summary = "Get all unique attribute keys of step items under specified project")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public List<String> getAttributeKeys(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestParam(value = DEFAULT_FILTER_PREFIX + EQ + CRITERIA_NAME, required = false)
      String launchName,
      @RequestParam(value = DEFAULT_FILTER_PREFIX + CNT + CRITERIA_ITEM_ATTRIBUTE_KEY)
      String value) {
    return ofNullable(launchName).filter(StringUtils::isNotBlank).map(
            name -> getTestItemHandler.getAttributeKeys(
                projectExtractor.extractMembershipDetails(user, projectKey), name, value))
        .orElseGet(Collections::emptyList);
  }

  @Transactional(readOnly = true)
  @GetMapping("/step/attribute/values")
  @ResponseStatus(OK)
  @Operation(summary = "Get all unique attribute values of step items under specified project")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public List<String> getAttributeValues(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestParam(value = DEFAULT_FILTER_PREFIX + EQ + CRITERIA_NAME, required = false)
      String launchName, @RequestParam(value = DEFAULT_FILTER_PREFIX + EQ
      + CRITERIA_ITEM_ATTRIBUTE_KEY, required = false) String key,
      @RequestParam(value = DEFAULT_FILTER_PREFIX + CNT + CRITERIA_ITEM_ATTRIBUTE_VALUE)
      String value) {
    return ofNullable(launchName).filter(StringUtils::isNotBlank).map(
            name -> getTestItemHandler.getAttributeValues(
                projectExtractor.extractMembershipDetails(user, projectKey), name, key, value))
        .orElseGet(Collections::emptyList);
  }

  @Transactional
  @PutMapping(value = "/info")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @ResponseStatus(OK)
  @Operation(summary = "Bulk update attributes and description")
  public OperationCompletionRS bulkUpdate(@PathVariable String projectKey,
      @RequestBody @Validated BulkInfoUpdateRQ bulkInfoUpdateRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateTestItemHandler.bulkInfoUpdate(bulkInfoUpdateRQ,
        projectExtractor.extractMembershipDetails(user, projectKey),
        user
    );
  }

  @Transactional
  @PutMapping("/{itemId}/update")
  @ResponseStatus(OK)
  @Operation(summary = "Update test item")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public OperationCompletionRS updateTestItem(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @PathVariable Long itemId,
      @RequestBody @Validated UpdateTestItemRQ rq) {
    return updateTestItemHandler.updateTestItem(
        projectExtractor.extractMembershipDetails(user, projectKey), itemId, rq, user);
  }

  @Transactional
  @PutMapping("/issue/link")
  @ResponseStatus(OK)
  @Operation(summary = "Attach external issue for specified test items")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public List<OperationCompletionRS> linkExternalIssues(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestBody @Validated LinkExternalIssueRQ rq) {
    return updateTestItemHandler.processExternalIssues(rq,
        projectExtractor.extractMembershipDetails(user, projectKey), user
    );
  }

  @Transactional
  @PutMapping("/issue/unlink")
  @ResponseStatus(OK)
  @Operation(summary = "Unlink external issue for specified test items")
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public List<OperationCompletionRS> unlinkExternalIssues(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestBody @Validated UnlinkExternalIssueRQ rq) {
    return updateTestItemHandler.processExternalIssues(rq,
        projectExtractor.extractMembershipDetails(user, projectKey), user
    );
  }

  @Transactional(readOnly = true)
  @GetMapping("/items")
  @ResponseStatus(OK)
  @Operation(summary = "Get test items by specified ids")
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public List<TestItemResource> getTestItems(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user, @RequestParam(value = "ids") Long[] ids) {
    return getTestItemHandler.getTestItems(ids,
        projectExtractor.extractMembershipDetails(user, projectKey), user
    );
  }

  @Transactional(readOnly = true)
  @GetMapping("/search")
  @ResponseStatus(OK)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Operation(summary = "Search test items by either name or attribute")
  public Page<TestItemResource> getTestItems(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user,
      @RequestParam(value = DEFAULT_FILTER_PREFIX + CNT
          + CRITERIA_NAME, required = false) String name,
      @RequestParam(value = "filter.has.compositeAttribute", required = false) String attribute,
      @RequestParam(value = "filter.in.status", required = false) String status,
      Pageable pageable) {
    return testCaseSearchHandler.searchTestItems(name, attribute, status, pageable,
        projectExtractor.extractMembershipDetails(user, projectKey));
  }
}
