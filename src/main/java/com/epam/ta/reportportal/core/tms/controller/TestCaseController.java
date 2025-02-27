package com.epam.ta.reportportal.core.tms.controller;

import com.epam.ta.reportportal.core.tms.dto.TestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TestCaseRS;
import com.epam.ta.reportportal.core.tms.service.TestCaseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;

@RestController
@RequestMapping("/project/{projectId}/tms/testcase")
@Tag(name = "Test Case", description = "Test Case API collection")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseService testCaseService;

    @PreAuthorize(ASSIGNED_TO_PROJECT)
    @GetMapping("/{testCaseId}")
    TestCaseRS getTestCaseById(@PathVariable("projectId") final long projectId,
                               @PathVariable("testCaseId") final long testCaseId) {
        return testCaseService.getTestCaseById(projectId,testCaseId);
    }

    @PreAuthorize(ASSIGNED_TO_PROJECT)
    @GetMapping("/")
    List<TestCaseRS> getTestCaseByProjectId(@PathVariable("projectId") final long projectId) {
        return testCaseService.getTestCaseByProjectId(projectId);
    }

    @PreAuthorize(ASSIGNED_TO_PROJECT)
    @PostMapping
    TestCaseRS createTestCase(@PathVariable("projectId") final long projectId,
                              @RequestBody @Valid final TestCaseRQ inputDto) {
        return testCaseService.createTestCase(inputDto);
    }

    @PreAuthorize(ASSIGNED_TO_PROJECT)
    @PutMapping("/{testCaseId}")
    TestCaseRS updateTestCase(@PathVariable("projectId") final long projectId,
                              @PathVariable("testCaseId") final long testCaseId,
                              @RequestBody final TestCaseRQ inputDto) {
        return testCaseService.updateTestCase(testCaseId, inputDto);
    }

}
