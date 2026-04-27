/*
 * Copyright 2026 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.launch.impl;

import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ReportPortalUserUtil.TEST_PROJECT_NAME;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil.getLaunch;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.item.impl.merge.strategy.LaunchMergeFactory;
import com.epam.ta.reportportal.core.item.impl.merge.strategy.MergeStrategyType;
import com.epam.ta.reportportal.core.item.merge.LaunchMergeStrategy;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.LaunchTypeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.launch.LaunchViewModel;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.reporting.LaunchResource;
import com.epam.ta.reportportal.ws.reporting.MergeLaunchesRQ;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Pavel_Bortnik
 */
@ExtendWith(MockitoExtension.class)
class MergeLaunchHandlerImplTest {

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private LaunchMergeFactory launchMergeFactory;

  @Mock
  private LaunchConverter launchConverter;

  @Mock
  private LogIndexer logIndexer;

  @InjectMocks
  private MergeLaunchHandlerImpl handler;

  private final ReportPortalUser adminUser =
      getRpUser("admin", UserRole.ADMINISTRATOR, ProjectRole.MEMBER, 1L);

  @BeforeEach
  void stubLaunchConverter() {
    ReflectionTestUtils.setField(launchConverter, "TO_RESOURCE",
        (Function<Launch, LaunchResource>) launch -> {
          LaunchViewModel model = new LaunchViewModel();
          model.setLaunchId(launch.getId());
          model.setLaunchType(launch.getLaunchType());
          return model;
        }
    );
  }

  @Test
  void mergeLaunchesRejectedWhenLaunchTypesDiffer() {
    Project project = new Project();
    project.setId(1L);
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    Launch automation = getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT).orElseThrow();
    automation.setId(1L);
    automation.setLaunchType(LaunchTypeEnum.AUTOMATION);

    Launch agentic = getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT).orElseThrow();
    agentic.setId(2L);
    agentic.setLaunchType(LaunchTypeEnum.AGENTIC);

    when(launchRepository.findAllById(any())).thenReturn(List.of(automation, agentic));

    MergeLaunchesRQ rq = mergeRequest(Set.of(1L, 2L));

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.mergeLaunches(extractProjectDetails(adminUser, TEST_PROJECT_NAME),
            adminUser, rq
        )
    );

    assertEquals(BAD_REQUEST_ERROR, ex.getErrorType());
    assertEquals(
        "Error in handled Request. Please, check specified parameters: 'Launches with different launch types cannot be merged.'",
        ex.getMessage());
    verifyNoInteractions(launchMergeFactory);
    verify(launchRepository, never()).deleteAll(any());
    verifyNoInteractions(logIndexer);
  }

  @Test
  void mergeLaunchesSucceedsWhenLaunchTypesMatch() {
    Project project = new Project();
    project.setId(1L);
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    Launch first = getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT).orElseThrow();
    first.setId(1L);
    first.setLaunchType(LaunchTypeEnum.AGENTIC);

    Launch second = getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT).orElseThrow();
    second.setId(2L);
    second.setLaunchType(LaunchTypeEnum.AGENTIC);

    when(launchRepository.findAllById(any())).thenReturn(List.of(first, second));

    Launch merged = new Launch();
    merged.setId(100L);
    merged.setStatistics(Collections.emptySet());
    merged.setLaunchType(LaunchTypeEnum.AGENTIC);

    LaunchMergeStrategy strategy = mock(LaunchMergeStrategy.class);
    when(launchMergeFactory.getLaunchMergeStrategy(MergeStrategyType.BASIC)).thenReturn(strategy);
    when(strategy.mergeLaunches(any(), any(), any(), any())).thenReturn(merged);

    MergeLaunchesRQ rq = mergeRequest(Set.of(1L, 2L));

    LaunchResource result = handler.mergeLaunches(
        extractProjectDetails(adminUser, TEST_PROJECT_NAME), adminUser, rq);

    assertEquals(LaunchTypeEnum.AGENTIC, ((LaunchViewModel) result).getLaunchType());
    verify(launchMergeFactory).getLaunchMergeStrategy(MergeStrategyType.BASIC);
    verify(strategy).mergeLaunches(any(), eq(adminUser), eq(rq), any());
    verify(launchRepository).deleteAll(any());
    verify(logIndexer).indexLaunchLogs(eq(merged), any());
  }

  private static MergeLaunchesRQ mergeRequest(Set<Long> launchIds) {
    MergeLaunchesRQ rq = new MergeLaunchesRQ();
    rq.setLaunches(new HashSet<>(launchIds));
    rq.setMergeStrategyType("BASIC");
    return rq;
  }
}
