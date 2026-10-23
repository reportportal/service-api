package com.epam.reportportal.base.core.tms.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;

import com.epam.reportportal.base.core.tms.dto.TmsFixPushRS;
import com.epam.reportportal.base.core.tms.dto.TmsReviewCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsReviewCommentRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseFixedVersionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseReviewRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.core.tms.pipeline.TmsTestCaseFixRoundService;
import com.epam.reportportal.base.core.tms.pipeline.TmsTestCasePushToAgentService;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseReviewCommentService;
import com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Review of AI test cases: comments of an editor, "Push to agent", and the fixed version the agent sends back.
 * <p>
 * Example: the editor posts a comment to {@code .../test-case/103/review/comment}, then
 * {@code POST .../test-case/103/review/push}; the job reads {@code GET .../review/push/7}, fixes the case and
 * answers with {@code POST .../test-case/103/fixed-version}.
 */
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/test-case/{testCaseId}")
@Tag(name = "Test Case Review", description = "Review comments and fix rounds of AI test cases")
@RequiredArgsConstructor
@Valid
public class TestCaseReviewController {

  private final TmsTestCaseReviewCommentService reviewCommentService;
  private final TmsTestCasePushToAgentService pushToAgentService;
  private final TmsTestCaseFixRoundService fixRoundService;
  private final ProjectExtractor projectExtractor;

  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @GetMapping("/review")
  @Operation(summary = "Get the review of a test case",
      description = "The review comments of the test case with their states, how many are not sent yet, and "
          + "whether the agent is fixing the case now.")
  @ApiResponse(responseCode = "200", description = "Successful operation")
  public TmsTestCaseReviewRS getReview(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") Long testCaseId, @AuthenticationPrincipal ReportPortalUser user) {
    return reviewCommentService.getReview(projectId(user, projectKey), testCaseId);
  }

  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @PostMapping("/review/comment")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Add a review comment",
      description = "Comments the precondition or a step of an AI test case. Not available while the agent is "
          + "fixing the case.")
  @ApiResponse(responseCode = "201", description = "Comment added")
  public TmsReviewCommentRS addComment(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") Long testCaseId, @RequestBody @Valid TmsReviewCommentRQ rq,
      @AuthenticationPrincipal ReportPortalUser user) {
    return reviewCommentService.addComment(projectId(user, projectKey), testCaseId, user.getUserId(), rq);
  }

  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @DeleteMapping("/review/comment/{commentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete an own review comment",
      description = "The author can delete a comment until it is pushed to the agent.")
  @ApiResponse(responseCode = "204", description = "Comment deleted")
  public void deleteComment(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") Long testCaseId, @PathVariable("commentId") Long commentId,
      @AuthenticationPrincipal ReportPortalUser user) {
    reviewCommentService.deleteOwnComment(projectId(user, projectKey), testCaseId, commentId, user.getUserId());
  }

  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @DeleteMapping("/review/comment")
  @Operation(summary = "Discard the unsent review comments",
      description = "Removes all comments of the test case that were not pushed to the agent.")
  @ApiResponse(responseCode = "200", description = "Comments discarded; the review as it is now")
  public TmsTestCaseReviewRS discardComments(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") Long testCaseId, @AuthenticationPrincipal ReportPortalUser user) {
    var projectId = projectId(user, projectKey);
    reviewCommentService.discardUnsentComments(projectId, testCaseId);
    return reviewCommentService.getReview(projectId, testCaseId);
  }

  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @PostMapping("/review/push")
  @Operation(summary = "Push the review comments to the agent",
      description = "Starts the fix job of the pipeline that produced the test case with all its unsent "
          + "comments and records a fix round. If the job cannot be started nothing is recorded and the "
          + "comments stay not sent.")
  @ApiResponse(responseCode = "200", description = "Fix round started")
  public TmsFixPushRS pushToAgent(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") Long testCaseId, @AuthenticationPrincipal ReportPortalUser user) {
    return pushToAgentService.push(projectId(user, projectKey), testCaseId, user.getUserId());
  }

  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @GetMapping("/review/push/{pushId}")
  @Operation(summary = "Get a fix round",
      description = "The fix round with the comments it carries; the fix job reads it to know what to apply.")
  @ApiResponse(responseCode = "200", description = "Successful operation")
  public TmsFixPushRS getPush(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") Long testCaseId, @PathVariable("pushId") Long pushId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return pushToAgentService.getPush(projectId(user, projectKey), testCaseId, pushId);
  }

  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @PostMapping("/fixed-version")
  @Operation(summary = "Report the fixed version of a test case",
      description = "Ends a fix round: the content becomes a new default version of the test case (the previous "
          + "version stays as history), its grade is stored, the case goes back to Draft and each pushed "
          + "comment is marked addressed or not. All in one call.")
  @ApiResponse(responseCode = "200", description = "The test case after the fix")
  public TmsTestCaseRS reportFixedVersion(@PathVariable("projectKey") String projectKey,
      @PathVariable("testCaseId") Long testCaseId, @RequestBody @Valid TmsTestCaseFixedVersionRQ rq,
      @AuthenticationPrincipal ReportPortalUser user) {
    return fixRoundService.applyFixedVersion(projectId(user, projectKey), testCaseId, rq);
  }

  private long projectId(ReportPortalUser user, String projectKey) {
    return projectExtractor
        .extractMembershipDetails(user, EntityUtils.normalizeId(projectKey))
        .getProjectId();
  }
}
