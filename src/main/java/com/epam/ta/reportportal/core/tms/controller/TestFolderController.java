package com.epam.ta.reportportal.core.tms.controller;

import com.epam.ta.reportportal.core.tms.dto.TestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TestFolderRS;
import com.epam.ta.reportportal.core.tms.service.TestFolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;

@RestController
@RequestMapping("/project/{projectId}/tms/folder")
@Tag(name = "Test Folder", description = "Test Folder API collection")
@RequiredArgsConstructor
public class TestFolderController {
    
    private final TestFolderService testFolderService;

    @PreAuthorize(ASSIGNED_TO_PROJECT)
    @PostMapping
    @Operation(summary = "Create Test Folder")
    TestFolderRS createTestFolder(@PathVariable("projectId") final long projectId,
                                  @RequestBody final TestFolderRQ inputDto) {
        return testFolderService.createFolder(projectId,inputDto);
    }

    @PreAuthorize(ASSIGNED_TO_PROJECT)
    @PutMapping("/{folderId}")
    @Operation(summary = "Update Test Folder")
    TestFolderRS updateTestFolder(@PathVariable("projectId") final long projectId,
                                  @PathVariable("folderId") final long folderId,
                                  @RequestBody final TestFolderRQ inputDto) {
        return testFolderService.updateFolder(projectId, folderId, inputDto);
    }

    @PreAuthorize(ASSIGNED_TO_PROJECT)
    @GetMapping("/{folderId}")
    @Operation(summary = "Get Test Folder by ID")
    TestFolderRS getTestFolderById(@PathVariable("projectId") final long projectId,
                                   @PathVariable("folderId") final long folderId) {
        return testFolderService.getFolderById(folderId);
    }

    @PreAuthorize(ASSIGNED_TO_PROJECT)
    @GetMapping("/")
    @Operation(summary = "Get Test Folders by project ID")
    List<TestFolderRS> getTestFolderByProjectId(@PathVariable("projectId") final long projectId) {
        return testFolderService.getFolderByProjectID(projectId);
    }
}
