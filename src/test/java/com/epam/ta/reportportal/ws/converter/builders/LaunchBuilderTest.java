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

package com.epam.ta.reportportal.ws.converter.builders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.epam.ta.reportportal.ws.reporting.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.reporting.Mode;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRQ;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LaunchBuilderTest {

  @Test
  void launchBuilder() {
    final String description = "description";
    final Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    final Instant endDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    final Long projectId = 1L;
    final ItemAttributeResource attributeResource = new ItemAttributeResource("key", "value");
    final Long userId = 2L;
    final String passed = "PASSED";
    final Mode mode = Mode.DEFAULT;

    final Launch launch = new LaunchBuilder().addDescription(description)
        .addEndTime(endDate)
        .addProject(projectId)
        .addAttribute(attributeResource)
        .addUserId(userId)
        .addStatus(passed)
        .addMode(mode)
        .get();

    assertEquals(description, launch.getDescription());
    assertEquals(endDate.truncatedTo(ChronoUnit.MILLIS),
        launch.getEndTime().truncatedTo(ChronoUnit.MILLIS));
    assertEquals(projectId, launch.getProjectId());
    assertTrue(launch.getAttributes().contains(new ItemAttribute("key", "value", false)));
    assertEquals(userId, launch.getUserId());
    assertEquals(passed, launch.getStatus().name());
    assertEquals(LaunchModeEnum.DEFAULT, launch.getMode());
  }

  @Test
  void addStartRqTest() {
    final StartLaunchRQ request = new StartLaunchRQ();
    final String uuid = "uuid";
    request.setUuid(uuid);
    request.setMode(Mode.DEFAULT);
    final String description = "description";
    request.setDescription(description);
    final String name = "name";
    request.setName(name);
    final Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    request.setStartTime(Instant.now());
    request.setAttributes(Sets.newHashSet(new ItemAttributesRQ("key", "value")));

    final Launch launch = new LaunchBuilder().addStartRQ(request)
        .addAttributes(request.getAttributes()).get();

    assertEquals(name, launch.getName());
    assertEquals(uuid, launch.getUuid());
    assertEquals(description, launch.getDescription());
    assertEquals(now.truncatedTo(ChronoUnit.SECONDS), launch.getStartTime().truncatedTo(ChronoUnit.SECONDS));
    assertTrue(launch.getAttributes().contains(new ItemAttribute("key", "value", false)));
    assertEquals(LaunchModeEnum.DEFAULT, launch.getMode());
  }

  @Test
  void overwriteAttributes() {
    Launch launch = new Launch();
    final ItemAttribute systemAttribute = new ItemAttribute("key", "value", true);
    launch.setAttributes(
        Sets.newHashSet(new ItemAttribute("key", "value", false), systemAttribute));

    final Launch buildLaunch = new LaunchBuilder(launch).overwriteAttributes(
        Sets.newHashSet(new com.epam.ta.reportportal.ws.reporting.ItemAttributeResource("newKey",
            "newVal"
        ))).get();

    assertThat(buildLaunch.getAttributes()).containsExactlyInAnyOrder(
        new ItemAttribute("newKey", "newVal", false), systemAttribute);
  }
}
