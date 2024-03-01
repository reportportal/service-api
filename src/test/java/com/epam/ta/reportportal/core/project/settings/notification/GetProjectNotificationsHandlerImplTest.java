/*
 * Copyright 2022 EPAM Systems
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

package com.epam.ta.reportportal.core.project.settings.notification;

import static com.epam.ta.reportportal.entity.enums.SendCase.ALWAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.entity.enums.LogicalOperator;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
class GetProjectNotificationsHandlerImplTest {

  private static final Long DEFAULT_PROJECT_ID = 1L;
  private static final Long DEFAULT_SENDER_CASE_1_ID = 1L;
  private static final Long DEFAULT_SENDER_CASE_2_ID = 2L;
  private static final String DEFAULT_SENDER_CASE_1_RULE_NAME = "rule #" + DEFAULT_SENDER_CASE_1_ID;
  private static final String DEFAULT_SENDER_CASE_2_RULE_NAME = "rule #" + DEFAULT_SENDER_CASE_2_ID;

  private final SenderCaseRepository senderCaseRepository = mock(SenderCaseRepository.class);
  private final GetProjectNotificationsHandlerImpl getProjectNotificationsHandler =
      new GetProjectNotificationsHandlerImpl(senderCaseRepository);

  @Test
  public void getProjectNotificationsTest() {
    SenderCase senderCase1 = mock(SenderCase.class);
    SenderCase senderCase2 = mock(SenderCase.class);

    when(senderCase1.getId()).thenReturn(DEFAULT_SENDER_CASE_1_ID);
    when(senderCase1.getRuleName()).thenReturn(DEFAULT_SENDER_CASE_1_RULE_NAME);
    when(senderCase1.getSendCase()).thenReturn(ALWAYS);
    when(senderCase2.getId()).thenReturn(DEFAULT_SENDER_CASE_2_ID);
    when(senderCase2.getRuleName()).thenReturn(DEFAULT_SENDER_CASE_2_RULE_NAME);
    when(senderCase2.getSendCase()).thenReturn(ALWAYS);
    when(senderCase1.getAttributesOperator()).thenReturn(LogicalOperator.AND);
    when(senderCase2.getAttributesOperator()).thenReturn(LogicalOperator.AND);

    when(senderCaseRepository.findAllByProjectId(DEFAULT_PROJECT_ID)).thenReturn(
        List.of(senderCase1, senderCase2));

    List<SenderCaseDTO> result = getProjectNotificationsHandler.getProjectNotifications(
        DEFAULT_PROJECT_ID);
    assertEquals(2, result.size());
  }
}
