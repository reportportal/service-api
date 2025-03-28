package com.epam.ta.reportportal.core.tms.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.service.TmsTestCaseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project/{projectId}/tms/test-case")
@Tag(name = "Test Case", description = "Test Case API collection")
@RequiredArgsConstructor
public class TestCaseController {

  private final TmsTestCaseService tmsTestCaseService;

  @PreAuthorize(ADMIN_ONLY)
  @GetMapping("/{testCaseId}")
  public TmsTestCaseRS getTestCaseById(@PathVariable("projectId") final long projectId,
                             @PathVariable("testCaseId") final long testCaseId) {
    return tmsTestCaseService.getById(projectId, testCaseId);
  }

  @PreAuthorize(ADMIN_ONLY)
  @GetMapping
  public List<TmsTestCaseRS> getTestCaseByProjectId(@PathVariable("projectId") final long projectId) {
    return tmsTestCaseService.getTestCaseByProjectId(projectId);
  }

  @PreAuthorize(ADMIN_ONLY)
  @PostMapping
  public TmsTestCaseRS createTestCase(@PathVariable("projectId") final long projectId,
                            @RequestBody @Valid final TmsTestCaseRQ inputDto) {
    return tmsTestCaseService.create(projectId, inputDto);
  }

  @PreAuthorize(ADMIN_ONLY)
  @PutMapping("/{testCaseId}")
  public TmsTestCaseRS updateTestCase(@PathVariable("projectId") final long projectId,
                            @PathVariable("testCaseId") final long testCaseId,
                            @RequestBody final TmsTestCaseRQ inputDto) {
    return tmsTestCaseService.update(projectId, testCaseId, inputDto);
  }

  @PreAuthorize(ADMIN_ONLY)
  @PatchMapping("/{testCaseId}")
  public TmsTestCaseRS patchTestCase(@PathVariable("projectId") final long projectId,
      @PathVariable("testCaseId") final long testCaseId,
      @RequestBody final TmsTestCaseRQ inputDto) {
    return tmsTestCaseService.patch(projectId, testCaseId, inputDto);
  }
}
