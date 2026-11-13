/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.base.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.attribute.Attribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectAttribute;
import com.epam.reportportal.base.job.service.BrokenLaunchInterruptService;
import com.google.common.collect.Sets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class InterruptBrokenLaunchesJobTest {

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private BrokenLaunchInterruptService brokenLaunchInterruptService;

  @InjectMocks
  private InterruptBrokenLaunchesJob interruptBrokenLaunchesJob;

  @Test
  void delegatesCandidateLaunchesToInterruptService() {
    Project project = projectWithInterruptTime();
    long launchId = 1L;

    when(projectRepository.findAllIdsAndProjectAttributes(any())).thenReturn(
        new PageImpl<>(Collections.singletonList(project)));
    when(launchRepository.findIdsWithStatusAndStartTimeBefore(any(), any(), any(),
        any())).thenReturn(Collections.singletonList(launchId));

    interruptBrokenLaunchesJob.execute(null);

    verify(brokenLaunchInterruptService).interruptIfStillBroken(launchId,
        project.getOrganizationId(),
        Duration.ofDays(1));

  }

  @Test
  void continuesCandidateProcessingWhenOneLaunchFails() {
    Project project = projectWithInterruptTime();
    long launchId = 1L;
    long nextLaunchId = 2L;

    when(projectRepository.findAllIdsAndProjectAttributes(any())).thenReturn(
        new PageImpl<>(Collections.singletonList(project)));
    when(launchRepository.findIdsWithStatusAndStartTimeBefore(any(), any(), any(),
        any())).thenReturn(List.of(launchId, nextLaunchId));
    doThrow(new RuntimeException("boom"))
        .when(brokenLaunchInterruptService)
        .interruptIfStillBroken(eq(launchId), any(), any());

    interruptBrokenLaunchesJob.execute(null);

    verify(brokenLaunchInterruptService).interruptIfStillBroken(launchId,
        project.getOrganizationId(), Duration.ofDays(1));
    verify(brokenLaunchInterruptService).interruptIfStillBroken(nextLaunchId,
        project.getOrganizationId(), Duration.ofDays(1));
  }

  @Test
  void shouldExcludeManualLaunchesFromInterruptQuery() {
    final Project project = new Project();
    final ProjectAttribute projectAttribute = new ProjectAttribute();
    final Attribute attribute = new Attribute();
    attribute.setName("job.interruptJobTime");
    projectAttribute.setAttribute(attribute);
    projectAttribute.setValue(String.valueOf(3600 * 24));
    project.setProjectAttributes(Sets.newHashSet(projectAttribute));

    when(projectRepository.findAllIdsAndProjectAttributes(any())).thenReturn(
        new PageImpl<>(Collections.singletonList(project)));
    when(launchRepository.findIdsWithStatusAndStartTimeBefore(any(), any(), any(),
        eq(LaunchTypeEnum.MANUAL))).thenReturn(Collections.emptyList());

    interruptBrokenLaunchesJob.execute(null);

    verify(launchRepository).findIdsWithStatusAndStartTimeBefore(any(), eq(StatusEnum.IN_PROGRESS),
        any(), eq(LaunchTypeEnum.MANUAL));
  }

  private Project projectWithInterruptTime() {
    Project project = new Project();
    final ProjectAttribute projectAttribute = new ProjectAttribute();
    final Attribute attribute = new Attribute();
    attribute.setName("job.interruptJobTime");
    projectAttribute.setAttribute(attribute);

    //1 day in seconds
    projectAttribute.setValue(String.valueOf(3600 * 24));
    project.setProjectAttributes(Sets.newHashSet(projectAttribute));
    project.setName("name");
    return project;
  }
}
