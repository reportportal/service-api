package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.dto.TestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TestCaseRS;
import com.epam.ta.reportportal.core.tms.exception.NotFoundException;
import com.epam.ta.reportportal.core.tms.mapper.DtoMapper;
import com.epam.ta.reportportal.dao.TmsTestCaseRepository;
import com.epam.ta.reportportal.dao.TmsTestFolderRepository;
import com.epam.ta.reportportal.entity.tms.TmsTestCase;
import com.epam.ta.reportportal.entity.tms.TmsTestCaseVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

import static com.epam.ta.reportportal.core.tms.service.TestFolderServiceImpl.TEST_FOLDER_NOT_FOUND_BY_ID;

@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private static final String TEST_CASE_NOT_FOUND_BY_ID = "Test Case cannot be found by id: {0}";

    private final DtoMapper<TmsTestCase, TestCaseRS> testCaseMapper;
    private final TmsTestCaseRepository testCaseRepository;
    private final TmsTestFolderRepository testFolderRepository;

    @Override
    public TestCaseRS createTestCase(final TestCaseRQ inputDto) {
        final var testFolder = testFolderRepository.findById(inputDto.testFolderId())
                .orElseThrow(NotFoundException.supplier(TEST_FOLDER_NOT_FOUND_BY_ID, inputDto.testFolderId())); // replace by getting default Test Folder
        final var testCase = new TmsTestCase(null,
                inputDto.name(),
                inputDto.description(),
                new HashSet<>(),
                new HashSet<>(),
                testFolder);
        testCase.addTestCaseVersion(new TmsTestCaseVersion(null, "Default", true, false, null));

        return testCaseMapper.convert(testCaseRepository.save(testCase));
    }

    @Override
    public TestCaseRS updateTestCase(final long testCaseId, final TestCaseRQ inputDto) {
        final var testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(NotFoundException.supplier(TEST_FOLDER_NOT_FOUND_BY_ID, testCaseId)); // replace by getting default Test Folder
        return null;
    }

    @Override
    public TestCaseRS getTestCaseById(long projectId, long id) {
        return testCaseMapper.convert(testCaseRepository.findById(id)
                .orElseThrow(NotFoundException.supplier(TEST_CASE_NOT_FOUND_BY_ID, id)));
    }
    @Override
    public List<TestCaseRS> getTestCaseByProjectId(long projectId) {
        return testCaseRepository.findByTestFolder_ProjectId(projectId).stream().map(testCaseMapper::convert).toList();
    }

}
