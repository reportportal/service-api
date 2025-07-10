package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsDataset;
import com.epam.ta.reportportal.core.tms.db.entity.TmsDatasetData;
import com.epam.ta.reportportal.core.tms.db.repository.TmsDatasetDataRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetDataRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsDatasetDataMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsDatasetDataServiceImplTest {

  @Mock
  private TmsDatasetDataMapper tmsDatasetDataMapper;

  @Mock
  private TmsDatasetDataRepository tmsDatasetDataRepository;

  @InjectMocks
  private TmsDatasetDataServiceImpl sut;

  @Test
  void shouldCreateDatasetData() {
    // Given
    var tmsDataset = new TmsDataset();
    tmsDataset.setId(1L);

    var firstTmsDatasetDataRQ = new TmsDatasetDataRQ();
    var secondTmsDatasetDataRQ = new TmsDatasetDataRQ();

    var firstData = new TmsDatasetData();
    var secondData = new TmsDatasetData();

    var tmsDatasetDataRQs = List.of(firstTmsDatasetDataRQ, secondTmsDatasetDataRQ);
    var tmsDatasetData = List.of(firstData, secondData);

    when(tmsDatasetDataMapper.convertToTmsDatasetData(tmsDatasetDataRQs)).thenReturn(
        tmsDatasetData);

    // When
    assertDoesNotThrow(() -> sut.createDatasetData(tmsDataset, tmsDatasetDataRQs));

    // Then
    verify(tmsDatasetDataMapper).convertToTmsDatasetData(tmsDatasetDataRQs);
    assertEquals(tmsDatasetData, tmsDataset.getData());
    verify(tmsDatasetDataRepository).saveAll(tmsDatasetData);

    tmsDatasetData.forEach(data -> assertEquals(tmsDataset, data.getDataset()));
  }

  @Test
  void shouldNotCreateDatasetDataWhenMissingInRequest() {
    // Given
    var tmsDataset = new TmsDataset();
    Collection<TmsDatasetDataRQ> tmsDatasetDataRQs = List.of(); // Empty collection

    // When
    assertDoesNotThrow(() -> sut.createDatasetData(tmsDataset, tmsDatasetDataRQs));

    // Then
    verifyNoInteractions(tmsDatasetDataMapper, tmsDatasetDataRepository);
  }

  @Test
  void shouldUpsertDatasetData() {
    // Given
    var tmsDataset = new TmsDataset();
    tmsDataset.setId(1L);

    var firstTmsDatasetDataRQ = new TmsDatasetDataRQ();
    var secondTmsDatasetDataRQ = new TmsDatasetDataRQ();

    var firstDatasetData = new TmsDatasetData();
    var secondDatasetData = new TmsDatasetData();

    List<TmsDatasetDataRQ> tmsDatasetDataRQs = List.of(firstTmsDatasetDataRQ,
        secondTmsDatasetDataRQ);
    List<TmsDatasetData> tmsDatasetData = List.of(firstDatasetData, secondDatasetData);

    when(tmsDatasetDataMapper.convertToTmsDatasetData(tmsDatasetDataRQs)).thenReturn(
        tmsDatasetData);

    // When
    assertDoesNotThrow(() -> sut.upsertDatasetData(tmsDataset, tmsDatasetDataRQs));

    // Then
    verify(tmsDatasetDataRepository).deleteAllByDataset_Id(tmsDataset.getId());
    verify(tmsDatasetDataMapper).convertToTmsDatasetData(tmsDatasetDataRQs);
    verify(tmsDatasetDataRepository).saveAll(tmsDatasetData);

    assertEquals(tmsDatasetData, tmsDataset.getData());
    tmsDatasetData.forEach(data -> assertEquals(tmsDataset, data.getDataset()));
  }

  @Test
  void shouldAddDatasetData() {
    // Given
    var tmsDataset = new TmsDataset();
    List<TmsDatasetData> existingDatasetData = new ArrayList<>();
    existingDatasetData.add(new TmsDatasetData());
    tmsDataset.setData(existingDatasetData);

    var firstTmsDatasetDataRQ = new TmsDatasetDataRQ();
    var secondTmsDatasetDataRQ = new TmsDatasetDataRQ();

    var firstDatasetData = new TmsDatasetData();
    var secondDatasetData = new TmsDatasetData();

    List<TmsDatasetDataRQ> tmsDatasetDataRQs = List.of(firstTmsDatasetDataRQ,
        secondTmsDatasetDataRQ);
    List<TmsDatasetData> newData = List.of(firstDatasetData, secondDatasetData);

    when(tmsDatasetDataMapper.convertToTmsDatasetData(tmsDatasetDataRQs)).thenReturn(newData);

    // When
    assertDoesNotThrow(() -> sut.addDatasetData(tmsDataset, tmsDatasetDataRQs));

    // Then
    verify(tmsDatasetDataMapper).convertToTmsDatasetData(tmsDatasetDataRQs);
    verify(tmsDatasetDataRepository).saveAll(newData);

    assertTrue(tmsDataset.getData().containsAll(existingDatasetData));
    assertTrue(tmsDataset.getData().containsAll(newData));
    newData.forEach(data -> assertEquals(tmsDataset, data.getDataset()));
  }

  @Test
  void shouldNotAddDatasetDataWhenMissingInRequest() {
    // Given
    var tmsDataset = new TmsDataset();
    Collection<TmsDatasetDataRQ> tmsDatasetDataRQs = List.of(); // Empty collection

    // When
    assertDoesNotThrow(() -> sut.addDatasetData(tmsDataset, tmsDatasetDataRQs));

    // Then
    verifyNoInteractions(tmsDatasetDataMapper, tmsDatasetDataRepository);
  }

  @Test
  void shouldDeleteByDatasetId() {
    // Given
    Long datasetId = 1L;

    // When
    assertDoesNotThrow(() -> sut.deleteByDatasetId(datasetId));

    // Then
    verify(tmsDatasetDataRepository).deleteAllByDataset_Id(datasetId);
  }
}
