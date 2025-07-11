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

package com.epam.ta.reportportal.core.item.impl;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.MembershipUtils.rpUserToMembership;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.reporting.async.producer.ItemFinishProducer;
import com.epam.ta.reportportal.ws.reporting.FinishTestItemRQ;
import java.util.UUID;
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
class FinishTestItemHandlerAsyncImplTest {

  @Mock
  AmqpTemplate amqpTemplate;

  @InjectMocks
  ItemFinishProducer finishTestItemHandlerAsync;

  @Test
  void finishTestItem() {
    FinishTestItemRQ request = new FinishTestItemRQ();
    request.setLaunchUuid(UUID.randomUUID().toString());
    ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR,
        1L);

    finishTestItemHandlerAsync.finishTestItem(user, rpUserToMembership(user),
        "123", request);
    verify(amqpTemplate).convertAndSend(any(), any(), any(), any());
  }

  @Test
  void finishTestItemWithoutLaunchUuid() {
    FinishTestItemRQ request = new FinishTestItemRQ();
    ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR,
        1L);

    ReportPortalException exception = assertThrows(
        ReportPortalException.class,
        () -> finishTestItemHandlerAsync.finishTestItem(user, rpUserToMembership(user), "123", request)
    );
    assertEquals(
        "Error in handled Request. Please, check specified parameters: 'Launch UUID should not be null or empty.'",
        exception.getMessage()
    );
  }
}
