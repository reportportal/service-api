/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.persistence.service.LogTypeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemLogServiceTest {

  @Mock
  private LogRepository logRepository;

  @Mock
  private LogTypeResolver logTypeResolver;

  @InjectMocks
  private SystemLogService systemLogService;

  @Test
  void writesLaunchLogWithLaunchAttachedAndTestItemNullAndReturnsId() {
    Launch launch = new Launch();
    launch.setId(42L);
    launch.setProjectId(7L);
    when(logTypeResolver.resolveLogLevelFromName(7L, "mobitru")).thenReturn(90000);
    when(logRepository.saveAndFlush(any(Log.class))).thenAnswer(inv -> {
      Log saved = inv.getArgument(0);
      saved.setId(123L);
      return saved;
    });

    Long id = systemLogService.writeLaunchLog(launch, "mobitru", "device-1");

    assertThat(id).isEqualTo(123L);
    ArgumentCaptor<Log> logCaptor = ArgumentCaptor.forClass(Log.class);
    verify(logRepository).saveAndFlush(logCaptor.capture());
    Log saved = logCaptor.getValue();
    assertThat(saved.getLaunch()).isSameAs(launch);
    assertThat(saved.getTestItem()).isNull();
    assertThat(saved.getProjectId()).isEqualTo(7L);
    assertThat(saved.getLogLevel()).isEqualTo(90000);
    assertThat(saved.getLogMessage()).isEqualTo("device-1");
    assertThat(saved.getUuid()).isNotBlank();
    assertThat(saved.getLogTime()).isNotNull();
  }

  @Test
  void writesTestItemLogWithTestItemAttachedAndLaunchNullOnRowAndReturnsId() {
    Launch launch = new Launch();
    launch.setId(42L);
    launch.setProjectId(7L);
    TestItem item = new TestItem();
    item.setItemId(10L);
    when(logTypeResolver.resolveLogLevelFromName(7L, "mobitru")).thenReturn(90000);
    when(logRepository.saveAndFlush(any(Log.class))).thenAnswer(inv -> {
      Log saved = inv.getArgument(0);
      saved.setId(456L);
      return saved;
    });

    Long id = systemLogService.writeTestItemLog(item, launch, "mobitru", "device-2");

    assertThat(id).isEqualTo(456L);
    ArgumentCaptor<Log> logCaptor = ArgumentCaptor.forClass(Log.class);
    verify(logRepository).saveAndFlush(logCaptor.capture());
    Log saved = logCaptor.getValue();
    assertThat(saved.getTestItem()).isSameAs(item);
    assertThat(saved.getLaunch()).isNull();
    assertThat(saved.getProjectId()).isEqualTo(7L);
    assertThat(saved.getLogLevel()).isEqualTo(90000);
    assertThat(saved.getLogMessage()).isEqualTo("device-2");
  }
}
