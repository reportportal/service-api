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

package com.epam.reportportal.base.ws.handler.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.ws.rabbit.QueryRQ;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Yauheni_Martynau
 */
@ExtendWith(MockitoExtension.class)
class QueryHandlerImplTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private IntegrationRepository integrationRepository;

  @Mock
  private TestItemRepository testItemRepository;

  @Mock
  private LogRepository logRepository;

  @InjectMocks
  private QueryHandlerImpl queryHandler;

  @Test
  void testFind_withLogRepositoryRequest() {

    //given:
    Filter requestFilter = Filter.builder()
        .withTarget(Log.class).withCondition(FilterCondition.builder().eq("id", "2").build())
        .build();

    QueryRQ queryRQ = new QueryRQ();
    queryRQ.setEntity(Log.class.getSimpleName());
    queryRQ.setFilter(requestFilter);

    //setup:
    when(logRepository.findByFilter(requestFilter)).thenReturn(Lists.newArrayList());

    //when:
    queryHandler.find(queryRQ);

    //then:
    ArgumentCaptor<Filter> captor = ArgumentCaptor.forClass(Filter.class);
    verify(logRepository).findByFilter(captor.capture());

    Filter capturedFilter = captor.getValue();

    assertEquals(requestFilter, capturedFilter);
  }

  @Test
  void testFind_withNotFoundRepository() {

    //given:
    Filter requestFilter = Filter.builder()
        .withTarget(Launch.class)
        .withCondition(FilterCondition.builder().eq("name", "name").build())
        .build();

    QueryRQ queryRQ = new QueryRQ();
    queryRQ.setEntity(Launch.class.getSimpleName());
    queryRQ.setFilter(requestFilter);

    //when:
    assertThrows(ReportPortalException.class, () -> queryHandler.find(queryRQ));
  }
}
