package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.exception.NotFoundException;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseServiceImplTest {

    @Mock
    private TmsTestCaseMapper tmsTestCaseMapper;

    @Mock
    private TmsTestCaseRepository tmsTestCaseRepository;

    @Mock
    private TmsTestCaseAttributeService tmsTestCaseAttributeService;

    @InjectMocks
    private TmsTestCaseServiceImpl sut;

    private TmsTestCaseRQ testCaseRQ;
    private TmsTestCase testCase;
    private TmsTestCaseRS testCaseRS;
    private List<TmsTestCaseAttributeRQ> attributes;
    private long projectId;
    private Long testCaseId;

    @BeforeEach
    void setUp() {
        projectId = 1L;
        testCaseId = 2L;

        attributes = new ArrayList<>();
        TmsTestCaseAttributeRQ attribute = new TmsTestCaseAttributeRQ();
        attribute.setValue("value");
        attribute.setAttributeId(3L);
        attributes.add(attribute);

        testCaseRQ = new TmsTestCaseRQ();
        testCaseRQ.setName("Test Case");
        testCaseRQ.setDescription("Description");
        testCaseRQ.setTestFolderId(4L);
        testCaseRQ.setTags(attributes);
        testCaseRQ.setDatasetId(5L);

        testCase = new TmsTestCase();
        testCase.setId(testCaseId);
        testCase.setName("Test Case");
        testCase.setDescription("Description");

        testCaseRS = new TmsTestCaseRS();
        testCaseRS.setId(testCaseId);
        testCaseRS.setName("Test Case");
        testCaseRS.setDescription("Description");
    }

    @Test
    void getTestCaseByProjectId_ShouldReturnListOfTestCases() {
        // Given
        List<TmsTestCase> testCases = List.of(testCase);
        when(tmsTestCaseRepository.findByTestFolder_ProjectId(projectId)).thenReturn(testCases);
        when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

        // When
        List<TmsTestCaseRS> result = sut.getTestCaseByProjectId(projectId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCaseRS, result.get(0));
        verify(tmsTestCaseRepository).findByTestFolder_ProjectId(projectId);
        verify(tmsTestCaseMapper).convert(testCase);
    }

    @Test
    void getById_WhenTestCaseExists_ShouldReturnTestCase() {
        // Given
        when(tmsTestCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testCase));
        when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

        // When
        TmsTestCaseRS result = sut.getById(projectId, testCaseId);

        // Then
        assertNotNull(result);
        assertEquals(testCaseRS, result);
        verify(tmsTestCaseRepository).findById(testCaseId);
        verify(tmsTestCaseMapper).convert(testCase);
    }

    @Test
    void getById_WhenTestCaseDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        when(tmsTestCaseRepository.findById(testCaseId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class,
            () -> sut.getById(projectId, testCaseId));
        verify(tmsTestCaseRepository).findById(testCaseId);
    }

    @Test
    void create_ShouldCreateAndReturnTestCase() {
        // Given
        when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ)).thenReturn(testCase);
        when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

        // When
        TmsTestCaseRS result = sut.create(projectId, testCaseRQ);

        // Then
        assertNotNull(result);
        assertEquals(testCaseRS, result);
        verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ);
        verify(tmsTestCaseRepository).save(testCase);
        verify(tmsTestCaseAttributeService).createTestCaseAttributes(testCase, attributes);
        verify(tmsTestCaseMapper).convert(testCase);
    }

    @Test
    void update_WhenTestCaseExists_ShouldUpdateAndReturnTestCase() {
        // Given
        TmsTestCase convertedTestCase = new TmsTestCase();
        when(tmsTestCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testCase));
        when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ)).thenReturn(convertedTestCase);
        when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

        // When
        TmsTestCaseRS result = sut.update(projectId, testCaseId, testCaseRQ);

        // Then
        assertNotNull(result);
        assertEquals(testCaseRS, result);
        verify(tmsTestCaseRepository).findById(testCaseId);
        verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ);
        verify(tmsTestCaseMapper).update(testCase, convertedTestCase);
        verify(tmsTestCaseAttributeService).updateTestCaseAttributes(testCase, attributes);
        verify(tmsTestCaseMapper).convert(testCase);
    }

    @Test
    void update_WhenTestCaseDoesNotExist_ShouldCreateNewTestCase() {
        // Given
        when(tmsTestCaseRepository.findById(testCaseId)).thenReturn(Optional.empty());
        when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ)).thenReturn(testCase);
        when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

        // When
        TmsTestCaseRS result = sut.update(projectId, testCaseId, testCaseRQ);

        // Then
        assertNotNull(result);
        assertEquals(testCaseRS, result);
        verify(tmsTestCaseRepository).findById(testCaseId);
        verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ);
        verify(tmsTestCaseRepository).save(testCase);
        verify(tmsTestCaseAttributeService).createTestCaseAttributes(testCase, attributes);
        verify(tmsTestCaseMapper).convert(testCase);
    }

    @Test
    void patch_WhenTestCaseExists_ShouldPatchAndReturnTestCase() {
        // Given
        TmsTestCase convertedTestCase = new TmsTestCase();
        when(tmsTestCaseRepository.findByIdAndProjectId(testCaseId, projectId)).thenReturn(Optional.of(testCase));
        when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ)).thenReturn(convertedTestCase);
        when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

        // When
        TmsTestCaseRS result = sut.patch(projectId, testCaseId, testCaseRQ);

        // Then
        assertNotNull(result);
        assertEquals(testCaseRS, result);
        verify(tmsTestCaseRepository).findByIdAndProjectId(testCaseId, projectId);
        verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ);
        verify(tmsTestCaseMapper).patch(testCase, convertedTestCase);
        verify(tmsTestCaseAttributeService).patchTestCaseAttributes(testCase, attributes);
        verify(tmsTestCaseMapper).convert(testCase);
    }

    @Test
    void patch_WhenTestCaseDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        when(tmsTestCaseRepository.findByIdAndProjectId(testCaseId, projectId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class,
            () -> sut.patch(projectId, testCaseId, testCaseRQ));
        verify(tmsTestCaseRepository).findByIdAndProjectId(testCaseId, projectId);
    }

    @Test
    void delete_ShouldDeleteTestCase() {
        // When
        sut.delete(projectId, testCaseId);

        // Then
        verify(tmsTestCaseAttributeService).deleteAllByTestCaseId(testCaseId);
        verify(tmsTestCaseRepository).deleteById(testCaseId);
    }
}
