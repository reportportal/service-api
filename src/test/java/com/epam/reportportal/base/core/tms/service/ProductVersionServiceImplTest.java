package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.ProductVersionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsProductVersionRS;
import com.epam.reportportal.base.core.tms.mapper.TmsProductVersionMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.ProductVersionRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsProductVersion;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductVersionServiceImplTest {

  @Mock
  private TmsProductVersionMapper productVersionMapper;

  @Mock
  private ProductVersionRepository productVersionRepository;

  @InjectMocks
  private ProductVersionServiceImpl sut;

  @Test
  void update_WhenVersionBelongsToProject_ShouldSaveWithThatProject() {
    long projectId = 1L;
    long versionId = 3L;
    var existing = new TmsProductVersion(versionId, "old", "old-doc", projectId);
    var request = new ProductVersionRQ(versionId, "versionUpdated", "docUpdated", 99L);
    var response = new TmsProductVersionRS();
    when(productVersionRepository.findByProjectIdAndId(projectId, versionId))
        .thenReturn(Optional.of(existing));
    when(productVersionRepository.save(any(TmsProductVersion.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(productVersionMapper.convert(any(TmsProductVersion.class))).thenReturn(response);

    var result = sut.update(projectId, versionId, request);

    assertEquals(response, result);
    var saved = ArgumentCaptor.forClass(TmsProductVersion.class);
    verify(productVersionRepository).save(saved.capture());
    assertEquals(versionId, saved.getValue().getId());
    assertEquals(projectId, saved.getValue().getProjectId());
    assertEquals("versionUpdated", saved.getValue().getVersion());
    assertEquals("docUpdated", saved.getValue().getDocumentation());
  }

  @Test
  void update_WhenVersionIsNotInProject_ShouldNotSave() {
    long projectId = 1L;
    long versionId = 3L;
    var request = new ProductVersionRQ(versionId, "versionUpdated", "docUpdated", projectId);
    when(productVersionRepository.findByProjectIdAndId(projectId, versionId))
        .thenReturn(Optional.empty());

    var exception = assertThrows(ReportPortalException.class,
        () -> sut.update(projectId, versionId, request));

    assertEquals(NOT_FOUND, exception.getErrorType());
    verify(productVersionRepository, never()).save(any());
  }
}
