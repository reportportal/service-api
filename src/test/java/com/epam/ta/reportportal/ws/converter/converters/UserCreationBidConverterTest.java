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

package com.epam.ta.reportportal.ws.converter.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.UserCreationBid;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQ;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class UserCreationBidConverterTest {

  @Test
  void toUser() {
    CreateUserRQ request = new CreateUserRQ();
    final String email = "email@example.com";
    request.setEmail(email);
    final String role = "role";
    request.setRole(role);
    final Project project = new Project();
    project.setName("projectName");
    final Date creationDate = Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant());
    project.setCreationDate(creationDate);

    final UserCreationBid bid = UserCreationBidConverter.TO_USER.apply(request, project);

    assertNotNull(bid.getUuid());
    assertEquals(bid.getEmail(), email);
    assertEquals(bid.getRole(), role);
    assertEquals(bid.getProjectName(), project.getName());
  }
}