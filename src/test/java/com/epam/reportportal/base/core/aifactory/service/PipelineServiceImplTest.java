package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineSettingsRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageSummaryRS;
import com.epam.reportportal.base.core.aifactory.mapper.PipelineMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.OffsetRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class PipelineServiceImplTest {

  @Mock
  private PipelineRepository pipelineRepository;
  @Mock
  private PipelineIterationRepository pipelineIterationRepository;
  @Mock
  private PipelineStageRepository pipelineStageRepository;
  @Mock
  private PipelineMapper pipelineMapper;

  @InjectMocks
  private PipelineServiceImpl sut;

  private Long projectId;
  private Long pipelineId;
  private Pipeline pipeline;

  @BeforeEach
  void setUp() {
    projectId = 1L;
    pipelineId = 2L;

    var project = new Project();
    project.setId(projectId);

    pipeline = new Pipeline();
    pipeline.setId(pipelineId);
    pipeline.setProject(project);
    pipeline.setName("factory-tc-gen");
    pipeline.setAutoReadyEnabled(false);
  }

  @Test
  void updateSettings_WhenEnablingWithValidThreshold_ShouldSaveAndReturnRS() {
    var rq = PipelineSettingsRQ.builder().autoReadyEnabled(true).autoReadyThreshold(90).build();
    var expectedRS = PipelineRS.builder().id(pipelineId).build();

    when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.save(pipeline)).thenReturn(pipeline);
    when(pipelineIterationRepository.countByPipelineId(pipelineId)).thenReturn(0L);
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipelineId))
        .thenReturn(Optional.empty());
    when(pipelineMapper.toPipelineRS(pipeline, 0L, null)).thenReturn(expectedRS);

    var result = sut.patchPipelineSettings(projectId, pipelineId, rq);

    assertEquals(expectedRS, result);
    assertEquals(true, pipeline.isAutoReadyEnabled());
    assertEquals(90, pipeline.getAutoReadyThreshold());
    verify(pipelineRepository).save(pipeline);
  }

  @Test
  void updateSettings_WhenEnablingWithoutThreshold_ShouldThrowBadRequest() {
    var rq = PipelineSettingsRQ.builder().autoReadyEnabled(true).autoReadyThreshold(null).build();
    when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));

    assertThrows(ReportPortalException.class, () -> sut.patchPipelineSettings(projectId, pipelineId, rq));
    verify(pipelineRepository, never()).save(any());
  }

  @Test
  void updateSettings_WhenEnablingWithOutOfRangeThreshold_ShouldThrowBadRequest() {
    var rq = PipelineSettingsRQ.builder().autoReadyEnabled(true).autoReadyThreshold(101).build();
    when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));

    assertThrows(ReportPortalException.class, () -> sut.patchPipelineSettings(projectId, pipelineId, rq));
    verify(pipelineRepository, never()).save(any());
  }

  @Test
  void updateSettings_WhenDisabling_ShouldNotRequireThreshold() {
    var rq = PipelineSettingsRQ.builder().autoReadyEnabled(false).autoReadyThreshold(null).build();
    var expectedRS = PipelineRS.builder().id(pipelineId).build();

    when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
    when(pipelineRepository.save(pipeline)).thenReturn(pipeline);
    when(pipelineIterationRepository.countByPipelineId(pipelineId)).thenReturn(0L);
    when(pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipelineId))
        .thenReturn(Optional.empty());
    when(pipelineMapper.toPipelineRS(pipeline, 0L, null)).thenReturn(expectedRS);

    var result = sut.patchPipelineSettings(projectId, pipelineId, rq);

    assertEquals(expectedRS, result);
    assertEquals(false, pipeline.isAutoReadyEnabled());
    verify(pipelineRepository).save(pipeline);
  }

  @Test
  void updateSettings_WhenPipelineBelongsToDifferentProject_ShouldThrowNotFound() {
    var otherProject = new Project();
    otherProject.setId(999L);
    pipeline.setProject(otherProject);
    var rq = PipelineSettingsRQ.builder().autoReadyEnabled(false).build();

    when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));

    assertThrows(ReportPortalException.class, () -> sut.patchPipelineSettings(projectId, pipelineId, rq));
    verify(pipelineRepository, never()).save(any());
  }

  @Test
  void updateSettings_WhenPipelineNotFound_ShouldThrowNotFound() {
    var rq = PipelineSettingsRQ.builder().autoReadyEnabled(false).build();
    when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.empty());

    assertThrows(ReportPortalException.class, () -> sut.patchPipelineSettings(projectId, pipelineId, rq));
    verify(pipelineRepository, never()).save(any());
  }

  @Test
  void listPipelines_ShouldBatchIterationCountsAndLatestIterationLookupsAcrossThePage() {
    var offsetRequest = new OffsetRequest(0, 50);
    var page = new PageImpl<>(List.of(pipeline));
    var latestIteration = new PipelineIteration();
    latestIteration.setId(42L);
    latestIteration.setPipeline(pipeline);
    var stage = new PipelineStage();
    stage.setId(501L);
    stage.setIteration(latestIteration);
    var stageSummary = PipelineStageSummaryRS.builder().id(501L).build();
    var summary = PipelineIterationSummaryRS.builder().id(42L).build();
    var expectedRS = PipelineRS.builder().id(pipelineId).build();

    when(pipelineRepository.findByProjectId(projectId, offsetRequest)).thenReturn(page);
    when(pipelineIterationRepository.countByPipelineIdIn(List.of(pipelineId)))
        .thenReturn(List.of(iterationCount(pipelineId, 3L)));
    when(pipelineIterationRepository.findLatestByPipelineIdIn(List.of(pipelineId)))
        .thenReturn(List.of(latestIteration));
    when(pipelineStageRepository.findByIterationIdInOrderByIterationIdAscSequenceAsc(List.of(42L)))
        .thenReturn(List.of(stage));
    when(pipelineMapper.toStageSummaryRS(stage)).thenReturn(stageSummary);
    when(pipelineMapper.toIterationSummaryRS(latestIteration, List.of(stageSummary))).thenReturn(summary);
    when(pipelineMapper.toPipelineRS(pipeline, 3L, summary)).thenReturn(expectedRS);

    var result = sut.getPipelines(projectId, offsetRequest);

    assertEquals(List.of(expectedRS), result.getContent());
    verify(pipelineIterationRepository, never()).countByPipelineId(any());
    verify(pipelineIterationRepository, never()).findFirstByPipelineIdOrderByIterationNumberDesc(any());
    verify(pipelineStageRepository, never()).countByIterationId(any());
    verify(pipelineStageRepository, never()).findByIterationIdOrderBySequenceAsc(any());
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
