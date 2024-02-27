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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_REPORT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER_OR_ADMIN;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.core.launch.util.LinkGenerator.composeBaseUrl;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.imprt.ImportLaunchHandler;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.launch.DeleteLaunchHandler;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.MergeLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.core.launch.StopLaunchHandler;
import com.epam.ta.reportportal.core.launch.UpdateLaunchHandler;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.content.ChartStatisticsContent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.model.BulkRQ;
import com.epam.ta.reportportal.model.DeleteBulkRQ;
import com.epam.ta.reportportal.model.DeleteBulkRS;
import com.epam.ta.reportportal.model.launch.AnalyzeLaunchRQ;
import com.epam.ta.reportportal.model.launch.FinishLaunchRS;
import com.epam.ta.reportportal.model.launch.LaunchImportRQ;
import com.epam.ta.reportportal.model.launch.UpdateLaunchRQ;
import com.epam.ta.reportportal.model.launch.cluster.CreateClustersRQ;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.BulkInfoUpdateRQ;
import com.epam.ta.reportportal.ws.model.launch.cluster.ClusterInfoResource;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import com.epam.ta.reportportal.ws.reporting.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.reporting.LaunchResource;
import com.epam.ta.reportportal.ws.reporting.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRQ;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRS;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import com.google.common.net.HttpHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller implementation for {@link com.epam.ta.reportportal.entity.launch.Launch} entity
 * <p>
 * Note: please use EntityUtils for forced lower case for user names and project names
 * </p>
 *
 * @author Andrei Varabyeu
 * @author Andrei Kliashchonak
 * @author Andrei_Ramanchuk
 */
@RestController
@RequestMapping("/v1/{projectName}/launch")
@Tag(name = "launch-controller", description = "Launch Controller")
public class LaunchController {

  private final ProjectExtractor projectExtractor;
  private final StartLaunchHandler startLaunchHandler;
  private final FinishLaunchHandler finishLaunchHandler;
  private final StopLaunchHandler stopLaunchHandler;
  private final DeleteLaunchHandler deleteLaunchMessageHandler;
  private final GetLaunchHandler getLaunchMessageHandler;
  private final UpdateLaunchHandler updateLaunchHandler;
  private final MergeLaunchHandler mergeLaunchesHandler;
  private final ImportLaunchHandler importLaunchHandler;
  private final GetJasperReportHandler<Launch> getJasperHandler;

  @Autowired
  public LaunchController(ProjectExtractor projectExtractor, StartLaunchHandler startLaunchHandler,
      FinishLaunchHandler finishLaunchHandler, StopLaunchHandler stopLaunchHandler,
      DeleteLaunchHandler deleteLaunchMessageHandler, GetLaunchHandler getLaunchMessageHandler,
      UpdateLaunchHandler updateLaunchHandler, MergeLaunchHandler mergeLaunchesHandler,
      ImportLaunchHandler importLaunchHandler,
      @Qualifier("launchJasperReportHandler") GetJasperReportHandler<Launch> getJasperHandler) {
    this.projectExtractor = projectExtractor;
    this.startLaunchHandler = startLaunchHandler;
    this.finishLaunchHandler = finishLaunchHandler;
    this.stopLaunchHandler = stopLaunchHandler;
    this.deleteLaunchMessageHandler = deleteLaunchMessageHandler;
    this.getLaunchMessageHandler = getLaunchMessageHandler;
    this.updateLaunchHandler = updateLaunchHandler;
    this.mergeLaunchesHandler = mergeLaunchesHandler;
    this.importLaunchHandler = importLaunchHandler;
    this.getJasperHandler = getJasperHandler;
  }

  /* Report client API */

  @PostMapping
  @PreAuthorize(ALLOWED_TO_REPORT)
  @ResponseStatus(CREATED)
  @Operation(summary = "Starts launch for specified project")
  public StartLaunchRS startLaunch(@PathVariable String projectName,
      @Parameter(description = "Start launch request body", required = true) @RequestBody @Validated
      @Valid StartLaunchRQ startLaunchRQ, @AuthenticationPrincipal ReportPortalUser user) {
    return startLaunchHandler.startLaunch(user,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), startLaunchRQ
    );
  }

  @PutMapping(value = "/{launchId}/finish")
  @PreAuthorize(ALLOWED_TO_REPORT)
  @ResponseStatus(OK)
  @Operation(summary = "Finish launch for specified project")
  public FinishLaunchRS finishLaunch(@PathVariable String projectName,
      @PathVariable String launchId, @RequestBody @Validated FinishExecutionRQ finishLaunchRQ,
      @AuthenticationPrincipal ReportPortalUser user, HttpServletRequest request) {
    return finishLaunchHandler.finishLaunch(launchId, finishLaunchRQ,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), user,
        composeBaseUrl(request)
    );
  }

  /* Frontend API */

  @Transactional
  @PutMapping("/{launchId}/stop")
  @PreAuthorize(ALLOWED_TO_REPORT)
  @ResponseStatus(OK)
  @Operation(summary = "Force finish launch for specified project")
  public OperationCompletionRS forceFinishLaunch(@PathVariable String projectName,
      @PathVariable Long launchId, @RequestBody @Validated FinishExecutionRQ finishExecutionRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return stopLaunchHandler.stopLaunch(launchId, finishExecutionRQ,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), user
    );
  }

  @Transactional
  @PutMapping("/stop")
  @PreAuthorize(ALLOWED_TO_REPORT)
  @ResponseStatus(OK)
  @Operation(summary = "Force finish launch")
  public List<OperationCompletionRS> bulkForceFinish(@PathVariable String projectName,
      @RequestBody @Validated BulkRQ<Long, FinishExecutionRQ> rq,
      @AuthenticationPrincipal ReportPortalUser user) {
    return stopLaunchHandler.stopLaunch(rq,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), user
    );
  }

  @Transactional
  @PutMapping("/{launchId}/update")
  @PreAuthorize(ALLOWED_TO_REPORT)
  @ResponseStatus(OK)
  @Operation(summary = "Updates launch for specified project")
  public OperationCompletionRS updateLaunch(@PathVariable String projectName,
      @PathVariable Long launchId, @RequestBody @Validated UpdateLaunchRQ updateLaunchRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateLaunchHandler.updateLaunch(launchId,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), user, updateLaunchRQ
    );
  }

  @Transactional
  @PutMapping("/update")
  @PreAuthorize(ALLOWED_TO_REPORT)
  @ResponseStatus(OK)
  @Operation(summary = "Updates launches for specified project")
  public List<OperationCompletionRS> updateLaunches(@PathVariable String projectName,
      @RequestBody @Validated BulkRQ<Long, UpdateLaunchRQ> rq,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateLaunchHandler.updateLaunch(rq,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), user
    );
  }

  @Transactional
  @DeleteMapping("/{launchId}")
  @PreAuthorize(ALLOWED_TO_REPORT)
  @ResponseStatus(OK)
  @Operation(summary = "Delete specified launch by ID")
  public OperationCompletionRS deleteLaunch(@PathVariable String projectName,
      @PathVariable Long launchId, @AuthenticationPrincipal ReportPortalUser user) {
    return deleteLaunchMessageHandler.deleteLaunch(launchId,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), user
    );
  }

  @Transactional(readOnly = true)
  @GetMapping("/{launchId}")
  @ResponseStatus(OK)
  @Operation(summary = "Get specified launch by ID")
  public LaunchResource getLaunch(@PathVariable String projectName, @PathVariable String launchId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLaunchMessageHandler.getLaunch(launchId,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName))
    );
  }

  @Transactional(readOnly = true)
  @GetMapping("/uuid/{launchId}")
  @ResponseStatus(OK)
  @Operation(summary = "Get specified launch by UUID")
  public LaunchResource getLaunchByUuid(@PathVariable String projectName,
      @PathVariable String launchId, @AuthenticationPrincipal ReportPortalUser user) {
    return getLaunchMessageHandler.getLaunch(launchId,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName))
    );
  }

  @Transactional(readOnly = true)
  @GetMapping
  @ResponseStatus(OK)
  @Operation(summary = "Get list of project launches by filter")
  public Iterable<LaunchResource> getProjectLaunches(@PathVariable String projectName,
      @FilterFor(Launch.class) Filter filter, @SortFor(Launch.class) Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLaunchMessageHandler.getProjectLaunches(
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), filter, pageable,
        user.getUsername()
    );
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/latest")
  @ResponseStatus(OK)
  @Operation(summary = "Get list of latest project launches by filter")
  public Iterable<LaunchResource> getLatestLaunches(@PathVariable String projectName,
      @FilterFor(Launch.class) Filter filter, @SortFor(Launch.class) Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLaunchMessageHandler.getLatestLaunches(
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), filter, pageable);
  }

  @GetMapping(value = "/mode")
  @ResponseBody
  @ResponseStatus(OK)
  @Operation(summary = "Get launches of specified project from DEBUG mode")
  public Iterable<LaunchResource> getDebugLaunches(@PathVariable String projectName,
      @FilterFor(Launch.class) Filter filter, @SortFor(Launch.class) Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLaunchMessageHandler.getDebugLaunches(
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), filter, pageable);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/attribute/keys")
  @ResponseStatus(OK)
  @Operation(summary = "Get all unique attribute keys of project launches")
  public List<String> getAttributeKeys(@PathVariable String projectName,
      @RequestParam(value = "filter." + "cnt." + "attributeKey") String value,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLaunchMessageHandler.getAttributeKeys(
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), value);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/attribute/values")
  @ResponseStatus(OK)
  @Operation(summary = "Get all unique attribute values of project launches")
  public List<String> getAttributeValues(@PathVariable String projectName,
      @RequestParam(value = "filter." + "eq." + "attributeKey", required = false) String key,
      @RequestParam(value = "filter." + "cnt." + "attributeValue") String value,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLaunchMessageHandler.getAttributeValues(
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), key, value);
  }

  @GetMapping(value = "/cluster/{launchId}")
  @ResponseStatus(OK)
  @Operation(summary = "Get all index clusters of the launch")
  public Iterable<ClusterInfoResource> getClusters(@PathVariable String projectName,
      @PathVariable String launchId, Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLaunchMessageHandler.getClusters(launchId,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), pageable
    );
  }

  @Transactional
  @PutMapping(value = "/info")
  @PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
  @ResponseStatus(OK)
  @Operation(summary = "Bulk update attributes and description")
  public OperationCompletionRS bulkUpdate(@PathVariable String projectName,
      @RequestBody @Validated BulkInfoUpdateRQ bulkInfoUpdateRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateLaunchHandler.bulkInfoUpdate(bulkInfoUpdateRQ,
        projectExtractor.extractProjectDetails(user, projectName)
    );
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/owners")
  @ResponseStatus(OK)
  @Operation(summary = "Get all unique owners of project launches")
  public List<String> getAllOwners(@PathVariable String projectName,
      @RequestParam(value = "filter." + "cnt." + "user") String value,
      @RequestParam(value = "mode", required = false, defaultValue = "DEFAULT") String mode,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getLaunchMessageHandler.getOwners(
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), value, mode);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/names")
  @ResponseStatus(OK)
  @Operation(summary = "Get launch names of project")
  public List<String> getAllLaunchNames(@PathVariable String projectName,
      @RequestParam(value = "filter." + "cnt." + "name", required = false, defaultValue = "")
      String value, @AuthenticationPrincipal ReportPortalUser user) {
    return getLaunchMessageHandler.getLaunchNames(
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), value);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/compare")
  @ResponseStatus(OK)
  @Operation(summary = "Compare launches")
  public Map<String, List<ChartStatisticsContent>> compareLaunches(@PathVariable String projectName,
      @RequestParam(value = "ids") Long[] ids, @AuthenticationPrincipal ReportPortalUser user) {
    return getLaunchMessageHandler.getLaunchesComparisonInfo(
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), ids);
  }

  @Transactional
  @PostMapping("/merge")
  @PreAuthorize(ALLOWED_TO_REPORT)
  @ResponseStatus(OK)
  @Operation(summary = "Merge set of specified launches in common one")
  public LaunchResource mergeLaunches(@PathVariable String projectName,
      @Parameter(description = "Merge launches request body", required = true) @RequestBody @Validated
          MergeLaunchesRQ mergeLaunchesRQ, @AuthenticationPrincipal ReportPortalUser user) {
    return mergeLaunchesHandler.mergeLaunches(
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), user,
        mergeLaunchesRQ
    );
  }

  @Transactional
  @PostMapping(value = "/analyze")
  @ResponseStatus(OK)
  @Operation(summary = "Start launch auto-analyzer on demand")
  public OperationCompletionRS startLaunchAnalyzer(@PathVariable String projectName,
      @RequestBody @Validated AnalyzeLaunchRQ analyzeLaunchRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateLaunchHandler.startLaunchAnalyzer(analyzeLaunchRQ,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), user
    );
  }

  @PostMapping(value = "/cluster")
  @ResponseStatus(OK)
  @Operation(summary = "Create launch clusters")
  public OperationCompletionRS createClusters(@PathVariable String projectName,
      @RequestBody @Validated CreateClustersRQ createClustersRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateLaunchHandler.createClusters(createClustersRQ,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), user
    );
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/status")
  @ResponseStatus(OK)

  public Map<String, String> getStatuses(@PathVariable String projectName,
      @RequestParam(value = "ids") Long[] ids, @AuthenticationPrincipal ReportPortalUser user) {
    return getLaunchMessageHandler.getStatuses(
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), ids);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/{launchId}/report")
  @ResponseStatus(OK)
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  @Operation(summary =  "Export specified launch", description = "Only following formats are supported: pdf (by default), xls, html.")
  public void getLaunchReport(@PathVariable String projectName, @PathVariable Long launchId,
      @Parameter(schema = @Schema(allowableValues = {"pdf", "xls", "html"}))
      @RequestParam(value = "view", required = false, defaultValue = "pdf") String view,
      @AuthenticationPrincipal ReportPortalUser user, HttpServletResponse response) {

    ReportFormat format = getJasperHandler.getReportFormat(view);
    response.setContentType(format.getContentType());

    response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
        String.format("attachment; filename=\"RP_LAUNCH_%s_Report.%s\"", format.name(),
            format.getValue()
        )
    );

    try (OutputStream outputStream = response.getOutputStream()) {
      getLaunchMessageHandler.exportLaunch(launchId, format, outputStream, user);
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Unable to write data to the response."
      );
    }
  }

  @Transactional
  @DeleteMapping
  @PreAuthorize(ALLOWED_TO_REPORT)
  @ResponseStatus(OK)
  @Operation(summary = "Delete specified launches by ids")
  public DeleteBulkRS deleteLaunches(@PathVariable String projectName,
      @RequestBody @Valid DeleteBulkRQ deleteBulkRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return deleteLaunchMessageHandler.deleteLaunches(deleteBulkRQ,
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), user
    );
  }

  @Parameters(
      @Parameter(name = "launchImportRq", content = @Content(schema = @Schema(implementation = LaunchImportRQ.class)))
  )
  @PostMapping(value = "/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
  @ResponseStatus(OK)
  @Operation(summary =  "Import junit xml report", description = "Only following formats are supported: zip and xml.")
  public OperationCompletionRS importLaunch(@PathVariable String projectName,
      @RequestParam("file") MultipartFile file, @AuthenticationPrincipal ReportPortalUser user,
      HttpServletRequest request,
      @RequestPart(required = false) @Valid LaunchImportRQ launchImportRq) {
    return importLaunchHandler.importLaunch(
        projectExtractor.extractProjectDetails(user, normalizeId(projectName)), user, "XUNIT", file,
        composeBaseUrl(request), launchImportRq
    );
  }
}
