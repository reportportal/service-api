package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.dto.TestFolderRS;
import com.epam.ta.reportportal.entity.tms.TmsTestFolder;
import org.springframework.stereotype.Component;

@Component
public class TestFolderMapper implements DtoMapper<TmsTestFolder, TestFolderRS> {

    public TestFolderRS convert(final TmsTestFolder testFolder) {
        return new TestFolderRS(testFolder.getId(), testFolder.getName(), testFolder.getDescription());
    }
}
