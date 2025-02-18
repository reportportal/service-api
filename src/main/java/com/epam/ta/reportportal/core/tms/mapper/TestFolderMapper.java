package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.model.TestFolder;
import com.epam.ta.reportportal.core.tms.dto.TestFolderRS;
import org.springframework.stereotype.Component;

@Component
public class TestFolderMapper implements DtoMapper<TestFolder, TestFolderRS> {

    public TestFolderRS convert(final TestFolder testFolder) {
        return new TestFolderRS(testFolder.getId(), testFolder.getName(), testFolder.getDescription());
    }
}
