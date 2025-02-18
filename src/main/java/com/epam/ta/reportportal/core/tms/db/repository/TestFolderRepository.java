package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.dao.ReportPortalRepository;

import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author Andrei_Varabyeu
 */
@Repository
public interface TestFolderRepository extends ReportPortalRepository<TmsTestFolder, Long> {
    /**
     * Find all folder for given project
     *
     * @param projectID ID of project
     * @return found folders
     */
    List<TmsTestFolder> findAllByProjectId(long projectID);

    /**
     * Finds a folder by given ID and project ID
     * @param id ID of folder
     * @param projectId ID of project
     * @return Test Folder
     */
    TmsTestFolder findByIdAndProjectId(long id, long projectId);
}
