package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsDataset;
import com.epam.ta.reportportal.core.tms.db.entity.TmsEnvironment;
import com.epam.ta.reportportal.core.tms.db.entity.TmsEnvironmentDataset;
import com.epam.ta.reportportal.core.tms.db.repository.TmsEnvironmentDatasetRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsEnvironmentDatasetRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsEnvironmentDatasetMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsEnvironmentDatasetServiceImplTest {

  @Mock
  private TmsEnvironmentDatasetRepository tmsEnvironmentDatasetRepository;

  @Mock
  private TmsEnvironmentDatasetMapper tmsEnvironmentDatasetMapper;

  @InjectMocks
  private TmsEnvironmentDatasetServiceImpl sut;

  @Test
  void testCreateEnvironmentDatasetWhenDatasetIsEmpty() {
    var tmsDataset = mock(TmsDataset.class);
    Collection<TmsEnvironmentDatasetRQ> environmentDatasetRQs = new ArrayList<>();

    assertDoesNotThrow(() -> sut.createEnvironmentDataset(tmsDataset, environmentDatasetRQs));

    verifyNoInteractions(tmsEnvironmentDatasetMapper);
    verifyNoInteractions(tmsEnvironmentDatasetRepository);
  }

  @Test
  void testCreateEnvironmentDataset() {
    var tmsDataset = mock(TmsDataset.class);
    var datasetRQ = mock(TmsEnvironmentDatasetRQ.class);
    Collection<TmsEnvironmentDatasetRQ> environmentDatasetRQs = List.of(datasetRQ);

    var tmsEnvironmentDataset = mock(TmsEnvironmentDataset.class);
    Set<TmsEnvironmentDataset> environments = Set.of(tmsEnvironmentDataset);

    when(
        tmsEnvironmentDatasetMapper.convertToEnvironmentDatasets(tmsDataset, environmentDatasetRQs))
        .thenReturn(environments);

    assertDoesNotThrow(() -> sut.createEnvironmentDataset(tmsDataset, environmentDatasetRQs));

    verify(tmsEnvironmentDatasetMapper).convertToEnvironmentDatasets(tmsDataset,
        environmentDatasetRQs);
    verify(tmsDataset).setEnvironmentDatasets(any());
    verify(tmsEnvironmentDatasetRepository).saveAll(environments);
  }

  @Test
  void testUpsertEnvironmentDataset() {
    var tmsDataset = mock(TmsDataset.class);
    Collection<TmsEnvironmentDatasetRQ> environmentDatasetRQs = List.of(
        mock(TmsEnvironmentDatasetRQ.class));

    when(tmsDataset.getId()).thenReturn(1L);

    assertDoesNotThrow(() -> sut.upsertEnvironmentDataset(tmsDataset, environmentDatasetRQs));

    verify(tmsEnvironmentDatasetRepository).deleteAllByDataset_Id(1L);
    verify(tmsEnvironmentDatasetMapper).convertToEnvironmentDatasets(tmsDataset,
        environmentDatasetRQs);
    verify(tmsEnvironmentDatasetRepository).saveAll(any());
  }

  @Test
  void testAddEnvironmentDatasetWhenDatasetIsEmpty() {
    TmsDataset tmsDataset = mock(TmsDataset.class);
    Collection<TmsEnvironmentDatasetRQ> environmentDatasetRQs = new ArrayList<>();

    sut.addEnvironmentDataset(tmsDataset, environmentDatasetRQs);

    verifyNoInteractions(tmsEnvironmentDatasetMapper);
    verifyNoInteractions(tmsEnvironmentDatasetRepository);
  }

  @Test
  void testAddEnvironmentDataset() {
    var tmsDataset = new TmsDataset();
    var tmsEnvironmentDatasetRQ = mock(TmsEnvironmentDatasetRQ.class);
    var tmsEnvironmentDatasetRQs = List.of(tmsEnvironmentDatasetRQ);
    var existingEnvironmentDataset = mock(TmsEnvironmentDataset.class);
    var existingEnvironmentDatasets = new HashSet<TmsEnvironmentDataset>();
    existingEnvironmentDatasets.add(existingEnvironmentDataset);
    tmsDataset.setEnvironmentDatasets(existingEnvironmentDatasets);

    var newEnvironmentDataset = mock(TmsEnvironmentDataset.class);
    var newEnvironmentsDatasets = new HashSet<TmsEnvironmentDataset>();
    newEnvironmentsDatasets.add(newEnvironmentDataset);

    when(tmsEnvironmentDatasetMapper.convertToEnvironmentDatasets(tmsDataset, tmsEnvironmentDatasetRQs))
        .thenReturn(newEnvironmentsDatasets);

    assertDoesNotThrow(() -> sut.addEnvironmentDataset(tmsDataset, tmsEnvironmentDatasetRQs));

    verify(tmsEnvironmentDatasetMapper).convertToEnvironmentDatasets(tmsDataset, tmsEnvironmentDatasetRQs);
    verify(tmsEnvironmentDatasetRepository).saveAll(newEnvironmentsDatasets);
    assertThat(existingEnvironmentDatasets)
        .isNotNull()
        .isNotEmpty()
        .contains(existingEnvironmentDataset, newEnvironmentDataset);
  }

  @Test
  void testDeleteByDatasetId() {
    Long datasetId = 1L;

    sut.deleteByDatasetId(datasetId);

    verify(tmsEnvironmentDatasetRepository).deleteAllByDataset_Id(datasetId);
  }
}
