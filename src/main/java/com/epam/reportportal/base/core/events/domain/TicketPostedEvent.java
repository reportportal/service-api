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

package com.epam.reportportal.base.core.events.domain;

import com.epam.reportportal.base.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.base.model.activity.TestItemActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Event published when a ticket is posted to a test item.
 *
 * @author Andrei Varabyeu
 */
@Setter
@Getter
@NoArgsConstructor
public class TicketPostedEvent extends AbstractEvent<Void> {

  private Ticket ticket;
  private TestItemActivityResource testItemActivityResource;

  /**
   * Constructs a TicketPostedEvent.
   *
   * @param ticket                   The ticket that was posted
   * @param userId                   The ID of the user who posted the ticket
   * @param userLogin                The login of the user who posted the ticket
   * @param testItemActivityResource The test item activity resource
   * @param orgId                    The organization ID
   */
  public TicketPostedEvent(Ticket ticket, Long userId, String userLogin,
      TestItemActivityResource testItemActivityResource, Long orgId) {
    super(userId, userLogin);
    this.ticket = ticket;
    this.testItemActivityResource = testItemActivityResource;
    this.organizationId = orgId;
  }
}
