package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineSettingsRQ;
import com.epam.reportportal.base.core.aifactory.mapper.PipelineMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineSettings;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.OffsetRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class PipelineServiceImplTest {

  @Mock
  private PipelineRepository pipelineRepository;
  @Mock
  private PipelineIterationRepository pipelineIterationRepository;
  @Mock
  private PipelineMapper pipelineMapper;
  @Mock
  private PipelineSettingsValidator settingsValidator;
  @Spy
  private List<PipelineSettingsValidator> settingsValidators = new ArrayList<>();

  @InjectMocks
  private PipelineServiceImpl sut;

  private Long projectId;
  private Long pipelineId;
  private Pipeline pipeline;

  @BeforeEach
  void setUp() {
    projectId = 1L;
    pipelineId = 2L;
    settingsValidators.add(settingsValidator);

    var project = new Project();
    project.setId(projectId);

    pipeline = new Pipeline();
    pipeline.setId(pipelineId);
    pipeline.setProject(project);
    pipeline.setName("factory-tc-gen");
  }

  private void givenPipelineIsSaved(PipelineRS expected) {
    when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.save(pipeline)).thenReturn(pipeline);
    when(pipelineIterationRepository.countByPipelineId(pipelineId)).thenReturn(0L);
    when(pipelineMapper.toPipelineRS(pipeline, 0L)).thenReturn(expected);
  }

  @Test
  void patchSettings_ShouldStoreTheGivenSettingsCheckThemAndReturnThePipeline() {
    var expectedRS = PipelineRS.builder().id(pipelineId).build();
    givenPipelineIsSaved(expectedRS);
    var autoReady = Map.of("enabled", true, "threshold", 90);
    var rq = PipelineSettingsRQ.builder().settings(Map.of("autoReady", autoReady)).build();

    var result = sut.patchPipelineSettings(projectId, pipelineId, rq);

    assertEquals(expectedRS, result);
    assertEquals(Map.of("autoReady", autoReady), pipeline.getSettings().getValues());
    verify(settingsValidator).validate(pipeline, Map.of("autoReady", autoReady));
    verify(pipelineRepository).save(pipeline);
  }

  @Test
  void patchSettings_ShouldReplaceNamedSettingsKeepTheOthersAndRemoveOnesSentAsNull() {
    givenPipelineIsSaved(PipelineRS.builder().id(pipelineId).build());
    pipeline.setSettings(new PipelineSettings(Map.of("autoReady", Map.of("enabled", false), "other", "kept",
        "obsolete", "x")));
    var changes = new HashMap<String, Object>();
    changes.put("autoReady", Map.of("enabled", true, "threshold", 80));
    changes.put("obsolete", null);

    sut.patchPipelineSettings(projectId, pipelineId, PipelineSettingsRQ.builder().settings(changes).build());

    assertEquals(Map.of("autoReady", Map.of("enabled", true, "threshold", 80), "other", "kept"),
        pipeline.getSettings().getValues());
  }

  @Test
  void patchSettings_WhenEverySettingIsRemoved_ShouldStoreNoSettings() {
    givenPipelineIsSaved(PipelineRS.builder().id(pipelineId).build());
    pipeline.setSettings(new PipelineSettings(Map.of("autoReady", Map.of("enabled", false))));
    var changes = new HashMap<String, Object>();
    changes.put("autoReady", null);

    sut.patchPipelineSettings(projectId, pipelineId, PipelineSettingsRQ.builder().settings(changes).build());

    assertNull(pipeline.getSettings());
  }

  @Test
  void patchSettings_WhenAValidatorRejectsThem_ShouldNotSave() {
    when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
    doThrow(new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "threshold is required"))
        .when(settingsValidator).validate(any(), any());
    var rq = PipelineSettingsRQ.builder().settings(Map.of("autoReady", Map.of("enabled", true))).build();

    assertThrows(ReportPortalException.class, () -> sut.patchPipelineSettings(projectId, pipelineId, rq));
    verify(pipelineRepository, never()).save(any());
  }

  @Test
  void patchSettings_WhenPipelineBelongsToDifferentProject_ShouldThrowNotFound() {
    var otherProject = new Project();
    otherProject.setId(999L);
    pipeline.setProject(otherProject);
    when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));

    assertThrows(ReportPortalException.class,
        () -> sut.patchPipelineSettings(projectId, pipelineId, PipelineSettingsRQ.builder().settings(Map.of()).build()));
    verify(pipelineRepository, never()).save(any());
  }

  @Test
  void patchSettings_WhenPipelineNotFound_ShouldThrowNotFound() {
    when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.empty());

    assertThrows(ReportPortalException.class,
        () -> sut.patchPipelineSettings(projectId, pipelineId, PipelineSettingsRQ.builder().settings(Map.of()).build()));
    verify(pipelineRepository, never()).save(any());
  }

  @Test
  void listPipelines_ShouldBatchIterationCountsAcrossThePage() {
    var offsetRequest = new OffsetRequest(0, 50);
    var page = new PageImpl<>(List.of(pipeline));
    var expectedRS = PipelineRS.builder().id(pipelineId).build();

    when(pipelineRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Pipeline>>any(), any(Pageable.class)))
        .thenReturn(page);
    when(pipelineIterationRepository.countByPipelineIdIn(List.of(pipelineId)))
        .thenReturn(List.of(iterationCount(pipelineId, 3L)));
    when(pipelineMapper.toPipelineRS(pipeline, 3L)).thenReturn(expectedRS);

    var result = sut.getPipelines(projectId, null, offsetRequest);

    assertEquals(List.of(expectedRS), result.getContent());
    verify(pipelineIterationRepository, never()).countByPipelineId(any());
  }

  @Test
  void listPipelines_WithoutIterations_ShouldStillReturnThePipelineWithZeroCount() {
    var page = new PageImpl<>(List.of(pipeline));
    var expectedRS = PipelineRS.builder().id(pipelineId).build();
    when(pipelineRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Pipeline>>any(), any(Pageable.class)))
        .thenReturn(page);
    when(pipelineIterationRepository.countByPipelineIdIn(List.of(pipelineId))).thenReturn(List.of());
    when(pipelineMapper.toPipelineRS(pipeline, 0L)).thenReturn(expectedRS);

    var result = sut.getPipelines(projectId, null, new OffsetRequest(0, 50));

    assertEquals(List.of(expectedRS), result.getContent());
  }

  private PipelineIterationRepository.PipelineIterationCount iterationCount(Long pipelineId, Long count) {
    return new PipelineIterationRepository.PipelineIterationCount() {
      @Override
      public Long getPipelineId() {
        return pipelineId;
      }

      @Override
      public Long getCount() {
        return count;
      }
    };
  }
}
