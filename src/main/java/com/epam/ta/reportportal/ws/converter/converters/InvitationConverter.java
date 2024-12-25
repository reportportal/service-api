/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationStatus;
import com.epam.ta.reportportal.entity.user.UserCreationBid;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Function;

public class InvitationConverter {

  private InvitationConverter() {
  }

  public static final Function<UserCreationBid, Invitation> TO_INVITATION = bid -> {
    var invitation = new Invitation();

    invitation.setId(UUID.fromString(bid.getUuid()));
    invitation.setEmail(bid.getEmail());
    invitation.setStatus(InvitationStatus.PENDING);
    invitation.setCreatedAt(bid.getLastModified());
    invitation.setExpiresAt(bid.getLastModified().plus(1, ChronoUnit.DAYS));
    invitation.setUserId(bid.getInvitingUser().getId());
    invitation.setFullName(bid.getInvitingUser().getFullName());

    return invitation;
  };
}
