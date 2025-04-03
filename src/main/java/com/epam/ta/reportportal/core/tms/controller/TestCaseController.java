package com.epam.ta.reportportal.core.tms.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.service.TmsTestCaseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project/{projectId}/tms/test-case")
@Tag(name = "Test Case", description = "Test Case API collection")
@RequiredArgsConstructor
public class TestCaseController {

  private final TmsTestCaseService tmsTestCaseService;

  @PreAuthorize(IS_ADMIN)
  @GetMapping("/{testCaseId}")
  public TmsTestCaseRS getTestCaseById(@PathVariable("projectId") final long projectId,
                             @PathVariable("testCaseId") final long testCaseId) {
    return tmsTestCaseService.getById(projectId, testCaseId);
  }

  @PreAuthorize(IS_ADMIN)
  @GetMapping
  public List<TmsTestCaseRS> getTestCaseByProjectId(@PathVariable("projectId") final long projectId) {
    return tmsTestCaseService.getTestCaseByProjectId(projectId);
  }

  @PreAuthorize(IS_ADMIN)
  @PostMapping
  public TmsTestCaseRS createTestCase(@PathVariable("projectId") final long projectId,
                            @RequestBody @Valid final TmsTestCaseRQ inputDto) {
    return tmsTestCaseService.create(projectId, inputDto);
  }

  @PreAuthorize(IS_ADMIN)
  @PutMapping("/{testCaseId}")
  public TmsTestCaseRS updateTestCase(@PathVariable("projectId") final long projectId,
                            @PathVariable("testCaseId") final long testCaseId,
                            @RequestBody final TmsTestCaseRQ inputDto) {
    return tmsTestCaseService.update(projectId, testCaseId, inputDto);
  }

  @PreAuthorize(IS_ADMIN)
  @PatchMapping("/{testCaseId}")
  public TmsTestCaseRS patchTestCase(@PathVariable("projectId") final long projectId,
      @PathVariable("testCaseId") final long testCaseId,
      @RequestBody final TmsTestCaseRQ inputDto) {
    return tmsTestCaseService.patch(projectId, testCaseId, inputDto);
  }
}
