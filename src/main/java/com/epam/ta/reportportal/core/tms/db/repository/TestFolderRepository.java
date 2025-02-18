package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.model.TestFolder;
import com.epam.ta.reportportal.dao.ReportPortalRepository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Andrei_Varabyeu
 */
@Repository
public interface TestFolderRepository extends ReportPortalRepository<TestFolder, Long> {
    /**
     * Find all folder for given project
     *
     * @param projectID ID of project
     * @return found folders
     */
    List<TestFolder> findAllByProjectId(long projectID);

    /**
     * Finds a folder by given ID and project ID
     * @param id ID of folder
     * @param projectId ID of project
     * @return Test Folder
     */
    TestFolder findByIdAndProjectId(long id, long projectId);
}
