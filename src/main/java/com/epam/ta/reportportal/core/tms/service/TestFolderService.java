package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.dto.TestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TestFolderRS;

import java.util.List;

public interface TestFolderService {
    
    TestFolderRS createFolder(final long projectId, final TestFolderRQ inputDto);
    TestFolderRS updateFolder(long projectId, long folderId, TestFolderRQ inputDto);
    TestFolderRS getFolderById(long id);
    List<TestFolderRS> getFolderByProjectID(long projectId);
}
