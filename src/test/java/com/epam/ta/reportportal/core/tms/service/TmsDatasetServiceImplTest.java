package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsDataset;
import com.epam.ta.reportportal.core.tms.db.repository.TmsDatasetRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRS;
import com.epam.ta.reportportal.core.tms.exception.NotFoundException;
import com.epam.ta.reportportal.core.tms.mapper.TmsDatasetMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class TmsDatasetServiceImplTest {

  @Mock
  private TmsDatasetRepository tmsDatasetRepository;

  @Mock
  private TmsDatasetMapper tmsDatasetMapper;

  @Mock
  private TmsDatasetDataService tmsDatasetDataService;

  @Mock
  private TmsEnvironmentDatasetService tmsEnvironmentDatasetService;

  @InjectMocks
  private TmsDatasetServiceImpl sut;

  @Test
  void shouldCreateDataset() {
    // Given
    long projectId = 1L;
    var tmsDatasetRQ = new TmsDatasetRQ();
    var tmsDataset = new TmsDataset();
    var tmsDatasetRS = new TmsDatasetRS();

    when(tmsDatasetMapper.convertFromRQ(projectId, tmsDatasetRQ)).thenReturn(tmsDataset);
    when(tmsDatasetRepository.save(tmsDataset)).thenReturn(tmsDataset);
    when(tmsDatasetMapper.convertToRS(tmsDataset)).thenReturn(tmsDatasetRS);

    // When
    var result = assertDoesNotThrow(() -> sut.create(projectId, tmsDatasetRQ));

    // Then
    verify(tmsDatasetMapper).convertFromRQ(projectId, tmsDatasetRQ);
    verify(tmsDatasetRepository).save(tmsDataset);
    verify(tmsDatasetDataService).createDatasetData(tmsDataset, tmsDatasetRQ.getAttributes());
    verify(tmsEnvironmentDatasetService)
        .createEnvironmentDataset(tmsDataset, tmsDatasetRQ.getEnvironmentAttachments());
    verify(tmsDatasetMapper).convertToRS(tmsDataset);

    assertEquals(tmsDatasetRS, result);
  }

  @Test
  void shouldGetDatasetById() {
    // Given
    long projectId = 1L;
    Long datasetId = 10L;
    var tmsDataset = new TmsDataset();
    var tmsDatasetRS = new TmsDatasetRS();

    when(tmsDatasetRepository.findByIdAndProjectId(datasetId, projectId)).thenReturn(
        java.util.Optional.of(tmsDataset));
    when(tmsDatasetMapper.convertToRS(tmsDataset)).thenReturn(tmsDatasetRS);

    // When
    var result = assertDoesNotThrow(() -> sut.getById(projectId, datasetId));

    // Then
    verify(tmsDatasetRepository).findByIdAndProjectId(datasetId, projectId);
    verify(tmsDatasetMapper).convertToRS(tmsDataset);
    assertEquals(tmsDatasetRS, result);
  }

  @Test
  void shouldNotGetDatasetByIdWhenNotFound() {
    // Given
    long projectId = 1L;
    Long datasetId = 10L;

    when(tmsDatasetRepository.findByIdAndProjectId(datasetId, projectId)).thenReturn(
        java.util.Optional.empty());

    // When
    assertThrows(NotFoundException.class,
        () -> sut.getById(projectId, datasetId));
    // Then
    verify(tmsDatasetRepository).findByIdAndProjectId(datasetId, projectId);
  }

  @Test
  void shouldDeleteDataset() {
    // Given
    long projectId = 1L;
    Long datasetId = 10L;

    // When
    assertDoesNotThrow(() -> sut.delete(projectId, datasetId));

    // Then
    verify(tmsDatasetDataService).deleteByDatasetId(datasetId);
    verify(tmsEnvironmentDatasetService).deleteByDatasetId(datasetId);
    verify(tmsDatasetRepository).deleteByIdAndProject_Id(datasetId, projectId);
  }

  @Test
  void shouldUploadDatasetFromFile() {
    long projectId = 1L;
    var file = mock(MultipartFile.class);

    var firstDatasetRQ = new TmsDatasetRQ();
    firstDatasetRQ.setName("test1");
    var secondDatasetRQ = new TmsDatasetRQ();
    secondDatasetRQ.setName("test2");

    var firstDataset = new TmsDataset();
    firstDataset.setId(1L);
    var secondsDataset = new TmsDataset();
    secondsDataset.setId(2L);

    var firstDatasetRS = new TmsDatasetRS();
    var secondDatasetRS = new TmsDatasetRS();

    when(tmsDatasetMapper.convertToRQ(file)).thenReturn(List.of(firstDatasetRQ, secondDatasetRQ));
    when(tmsDatasetMapper.convertFromRQ(projectId, firstDatasetRQ)).thenReturn(firstDataset);
    when(tmsDatasetMapper.convertFromRQ(projectId, secondDatasetRQ)).thenReturn(secondsDataset);
    when(tmsDatasetRepository.save(firstDataset)).thenReturn(firstDataset);
    when(tmsDatasetRepository.save(secondsDataset)).thenReturn(secondsDataset);
    when(tmsDatasetMapper.convertToRS(firstDataset)).thenReturn(firstDatasetRS);
    when(tmsDatasetMapper.convertToRS(secondsDataset)).thenReturn(secondDatasetRS);

    var result = assertDoesNotThrow(() -> sut.uploadFromFile(projectId, file));

    verify(tmsDatasetMapper).convertToRQ(file);

    verify(tmsDatasetMapper).convertFromRQ(projectId, firstDatasetRQ);
    verify(tmsDatasetMapper).convertFromRQ(projectId, secondDatasetRQ);

    verify(tmsDatasetRepository).save(firstDataset);
    verify(tmsDatasetRepository).save(secondsDataset);

    verify(tmsDatasetDataService).createDatasetData(firstDataset, firstDatasetRQ.getAttributes());
    verify(tmsDatasetDataService).createDatasetData(secondsDataset,
        secondDatasetRQ.getAttributes());

    verify(tmsDatasetMapper).convertToRS(firstDataset);
    verify(tmsDatasetMapper).convertToRS(secondsDataset);

    assertEquals(List.of(firstDatasetRS, secondDatasetRS), result);
  }

  @Test
  void testUpdate_ExistingDataset() {
    // Arrange
    long projectId = 1L;
    long datasetId = 100L;
    TmsDatasetRQ tmsDatasetRQ = mock(TmsDatasetRQ.class);
    TmsDataset existingDataset = mock(TmsDataset.class);
    TmsDataset updatedDataset = mock(TmsDataset.class);
    TmsDatasetRS expectedResponse = mock(TmsDatasetRS.class);

    when(tmsDatasetRepository.findByIdAndProjectId(datasetId, projectId)).thenReturn(
        Optional.of(existingDataset));
    when(tmsDatasetMapper.convertFromRQ(projectId, tmsDatasetRQ)).thenReturn(updatedDataset);
    when(tmsDatasetMapper.convertToRS(existingDataset)).thenReturn(expectedResponse);

    // Act
    TmsDatasetRS result = sut.update(projectId, datasetId, tmsDatasetRQ);

    // Assert
    verify(tmsDatasetMapper).update(existingDataset, updatedDataset);
    verify(tmsDatasetDataService).upsertDatasetData(existingDataset, tmsDatasetRQ.getAttributes());
    verify(tmsEnvironmentDatasetService).upsertEnvironmentDataset(existingDataset,
        tmsDatasetRQ.getEnvironmentAttachments());
    assertEquals(expectedResponse, result);
  }

  @Test
  void testUpdate_DatasetDoesNotExist() {
    // Arrange
    long projectId = 1L;
    long datasetId = 100L;
    var tmsDatasetRQ = mock(TmsDatasetRQ.class);
    var createdDataset = mock(TmsDataset.class);
    var expectedResponse = mock(TmsDatasetRS.class);

    when(tmsDatasetRepository.findByIdAndProjectId(datasetId, projectId)).thenReturn(
        Optional.empty());
    when(tmsDatasetMapper.convertFromRQ(projectId, tmsDatasetRQ)).thenReturn(createdDataset);
    when(tmsDatasetRepository.save(createdDataset)).thenReturn(createdDataset);
    when(tmsDatasetMapper.convertToRS(createdDataset)).thenReturn(expectedResponse);

    // Act
    var result = assertDoesNotThrow(() -> sut.update(projectId, datasetId, tmsDatasetRQ));

    // Assert
    verify(tmsDatasetRepository).findByIdAndProjectId(datasetId, projectId);
    verify(tmsDatasetMapper).convertFromRQ(projectId, tmsDatasetRQ);
    verify(tmsDatasetRepository).save(createdDataset);
    verify(tmsDatasetDataService).createDatasetData(createdDataset, tmsDatasetRQ.getAttributes());
    verify(tmsEnvironmentDatasetService)
        .createEnvironmentDataset(createdDataset, tmsDatasetRQ.getEnvironmentAttachments());
    verify(tmsDatasetMapper).convertToRS(createdDataset);

    assertEquals(expectedResponse, result);
  }

  @Test
  void testPatch_ExistingDataset() {
    // Arrange
    long projectId = 1L;
    long datasetId = 100L;
    TmsDatasetRQ tmsDatasetRQ = mock(TmsDatasetRQ.class);
    TmsDataset existingDataset = mock(TmsDataset.class);
    TmsDataset patchedDataset = mock(TmsDataset.class);
    TmsDatasetRS expectedResponse = mock(TmsDatasetRS.class);

    when(tmsDatasetRepository.findByIdAndProjectId(datasetId, projectId)).thenReturn(
        Optional.of(existingDataset));
    when(tmsDatasetMapper.convertFromRQ(projectId, tmsDatasetRQ)).thenReturn(patchedDataset);
    when(tmsDatasetMapper.convertToRS(existingDataset)).thenReturn(expectedResponse);

    // Act
    TmsDatasetRS result = sut.patch(projectId, datasetId, tmsDatasetRQ);

    // Assert
    verify(tmsDatasetMapper).patch(existingDataset, patchedDataset);
    verify(tmsDatasetDataService).addDatasetData(existingDataset, tmsDatasetRQ.getAttributes());
    verify(tmsEnvironmentDatasetService).addEnvironmentDataset(existingDataset,
        tmsDatasetRQ.getEnvironmentAttachments());
    assertEquals(expectedResponse, result);
  }

  @Test
  void testPatch_DatasetDoesNotExist_ShouldThrowNotFoundException() {
    // Arrange
    long projectId = 1L;
    long datasetId = 100L;
    TmsDatasetRQ tmsDatasetRQ = mock(TmsDatasetRQ.class);

    when(tmsDatasetRepository.findByIdAndProjectId(datasetId, projectId)).thenReturn(
        Optional.empty());

    // Act & Assert
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> sut.patch(projectId, datasetId, tmsDatasetRQ));

    assertEquals("TMS dataset cannot be found by id: 100 for project: 1", exception.getMessage());
  }
}
