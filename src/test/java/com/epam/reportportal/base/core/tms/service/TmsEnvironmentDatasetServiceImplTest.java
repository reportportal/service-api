package com.epam.reportportal.base.core.tms.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsDataset;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsEnvironmentDataset;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsEnvironmentDatasetRepository;
import com.epam.reportportal.base.core.tms.dto.TmsEnvironmentDatasetRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsEnvironmentDatasetMapper;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
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
  void testCreateEnvironmentDatasetWithEnvironmentOfAnotherProject_ThrowsNotFound() {
    var project = new Project();
    project.setId(1L);
    var tmsDataset = new TmsDataset();
    tmsDataset.setProject(project);
    var datasetRQ = new TmsEnvironmentDatasetRQ();
    datasetRQ.setEnvironmentId("77");

    when(tmsEnvironmentDatasetRepository.countEnvironmentsByIdsAndProjectId(Set.of(77L), 1L))
        .thenReturn(0L);

    var exception = assertThrows(ReportPortalException.class,
        () -> sut.createEnvironmentDataset(tmsDataset, List.of(datasetRQ)));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verifyNoInteractions(tmsEnvironmentDatasetMapper);
    verify(tmsEnvironmentDatasetRepository, never()).saveAll(anyCollection());
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

    when(tmsEnvironmentDatasetMapper.convertToEnvironmentDatasets(tmsDataset,
        tmsEnvironmentDatasetRQs))
        .thenReturn(newEnvironmentsDatasets);

    assertDoesNotThrow(() -> sut.addEnvironmentDataset(tmsDataset, tmsEnvironmentDatasetRQs));

    verify(tmsEnvironmentDatasetMapper).convertToEnvironmentDatasets(tmsDataset,
        tmsEnvironmentDatasetRQs);
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
