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

package com.epam.ta.reportportal.core.launch.impl;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.reporting.async.producer.LaunchFinishProducer;
import com.epam.ta.reportportal.ws.reporting.FinishExecutionRQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

/**
 * @author Konstantin Antipin
 */

@ExtendWith(MockitoExtension.class)
class FinishLaunchHandlerAsyncImplTest {

  @Mock
  AmqpTemplate amqpTemplate;

  @InjectMocks
  LaunchFinishProducer finishLaunchHandlerAsync;

  @Test
  void finishLaunch() {
    FinishExecutionRQ request = new FinishExecutionRQ();
    ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER,
        1L);

    finishLaunchHandlerAsync.finishLaunch("0", request,
        user.getProjectDetails().get("test_project"), user, "http://base");
    verify(amqpTemplate).convertAndSend(any(), any(), any(), any());
  }
}