package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.tms.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.ta.reportportal.model.Page;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Pageable;

public interface TmsTestFolderService extends CrudService<TmsTestFolderRQ, TmsTestFolderRS, Long> {

  Page<TmsTestFolderRS> getFoldersByCriteria(long projectId, Filter filter, Pageable pageable);

  Page<TmsTestFolderRS> getSubFolders(long projectId, Long folderId, Pageable pageable);

  void exportFolderById(Long projectId, Long folderId, TmsTestFolderExportFileType fileType,
      HttpServletResponse response);

  TmsTestFolderRS create(long projectId, NewTestFolderRQ testFolderRQ);

  Boolean existsById(long projectId, Long testFolderId);

  void resolveTestFolderRQ(TmsTestCaseRQ testCaseRequest, Long testFolderId, String testFolderName);

  /**
   * Resolves target folder.
   * Either creates a new folder or validates existing folder.
   *
   * @param projectId The ID of the project.
   * @param testFolderId The ID of existing folder (optional).
   * @param testFolderRQ The new folder information (optional).
   * @return The ID of the target folder.
   */
  Long resolveTargetFolderId(long projectId, Long testFolderId, NewTestFolderRQ testFolderRQ);

  TmsTestFolder getEntityById(long projectId, Long testFolderId);
}
