package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.ta.reportportal.model.Page;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Pageable;

public interface TmsTestFolderService extends CrudService<TmsTestFolderRQ, TmsTestFolderRS, Long> {

  Page<TmsTestFolderRS> getFoldersByCriteria(long projectId, Long testPlanId, Pageable pageable);

  Page<TmsTestFolderRS> getSubFolders(long projectId, Long folderId, Pageable pageable);

  void exportFolderById(Long projectId, Long folderId, TmsTestFolderExportFileType fileType,
      HttpServletResponse response);

  TmsTestFolderRS create(long projectId, String testFolderName);

  Boolean existsById(long projectId, Long testFolderId);

  TmsTestCaseTestFolderRQ resolveTestFolderRQ(Long testFolderId, String testFolderName);
}
