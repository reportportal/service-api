/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.util.sample;

import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.RandomStringUtils;

public final class LaunchSampleUtil {

  private LaunchSampleUtil() {
  }

  public static Launch getSampleLaunch(String uuid) {
    var launch = new Launch();
    launch.setUuid(uuid);
    launch.setName(RandomStringUtils.random(10));
    launch.setNumber(ThreadLocalRandom.current().nextLong(100));
    return launch;
  }

  public static Launch getSampleLaunch() {
    return getSampleLaunch(UUID.randomUUID().toString());
  }
}
