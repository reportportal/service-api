package com.epam.ta.reportportal.core.tms.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;

import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.service.TmsTestPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project/{projectId}/tms/test-plan")
@Tag(name = "Test Plan", description = "Test Plan API collection")
@RequiredArgsConstructor
public class TmsTestPlanController {

  private final TmsTestPlanService tmsTestPlanService;

  @PostMapping
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  public TmsTestPlanRS createTestPlan(@PathVariable Long projectId,
      @RequestBody TmsTestPlanRQ testPlan) {
    return tmsTestPlanService.create(projectId, testPlan);
  }

  @GetMapping
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  public Page<TmsTestPlanRS> getTestPlansByCriteria(
      @PathVariable Long projectId,
      @RequestParam(required = false) List<Long> environmentId,
      @RequestParam(required = false) List<Long> productVersionId,
      Pageable pageable) {
    return tmsTestPlanService.getByCriteria(
        projectId, environmentId, productVersionId, pageable
    );
  }

  @PutMapping("/{id}")
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  public TmsTestPlanRS updateTestPlan(@PathVariable Long projectId,
      @PathVariable("id") Long testPlanId,
      @RequestBody TmsTestPlanRQ testPlan) {
    return tmsTestPlanService.update(projectId, testPlanId, testPlan);
  }

  @GetMapping("/{id}")
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  public TmsTestPlanRS getTestPlanById(@PathVariable Long projectId,
      @PathVariable("id") Long testPlanId) {
    return tmsTestPlanService.getById(projectId, testPlanId);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  public void deleteTestPlan(@PathVariable Long projectId, @PathVariable("id") Long testPlanId) {
    tmsTestPlanService.delete(projectId, testPlanId);
  }

  @PatchMapping("/{id}")
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  public TmsTestPlanRS patchTestPlan(@PathVariable Long projectId,
      @PathVariable("id") Long testPlanId,
      @RequestBody TmsTestPlanRQ updatedTestPlan) {
    return tmsTestPlanService.patch(projectId, testPlanId, updatedTestPlan);
  }
}
