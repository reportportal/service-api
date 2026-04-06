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

package com.epam.ta.reportportal.core.launch.changes;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

import com.epam.ta.reportportal.core.item.repository.TestItemLastModifiedRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Detects changes in tracked {@link Launch} fields and triggers {@code test_item.last_modified}
 * updates for all items belonging to that launch.
 */
@Component
@RequiredArgsConstructor
public class LaunchFieldChangeCapture {

  private final TestItemLastModifiedRepository testItemLastModifiedRepository;

  /**
   * Captures the current state of tracked fields.
   */
  public LaunchChangesSnapshot capture(Launch launch) {
    Set<AttributeSnapshot> attrs = ofNullable(launch.getAttributes())
        .map(a -> a.stream()
            .map(ia -> new AttributeSnapshot(ia.getKey(), ia.getValue(), ia.isSystem()))
            .collect(toSet()))
        .orElse(Set.of());

    return new LaunchChangesSnapshot(
        launch.getDescription(),
        launch.getMode(),
        launch.getStatus(),
        launch.getRetentionPolicy(),
        attrs
    );
  }

  /**
   * Compares the current launch state against a previously taken snapshot. If any tracked field
   * differs, updates {@code last_modified} on every test item of the launch.
   */
  public void handleIfChanged(Launch launch, LaunchChangesSnapshot before) {
    if (hasChanges(launch, before)) {
      testItemLastModifiedRepository.updateLastModifiedByLaunchId(launch.getId());
    }
  }

  private boolean hasChanges(Launch launch, LaunchChangesSnapshot before) {
    if (!Objects.equals(before.description(), launch.getDescription())) {
      return true;
    }
    if (!Objects.equals(before.mode(), launch.getMode())) {
      return true;
    }
    if (!Objects.equals(before.status(), launch.getStatus())) {
      return true;
    }
    if (!Objects.equals(before.retentionPolicy(), launch.getRetentionPolicy())) {
      return true;
    }
    Set<AttributeSnapshot> currentAttrs = ofNullable(launch.getAttributes())
        .map(a -> a.stream()
            .map(ia -> new AttributeSnapshot(ia.getKey(), ia.getValue(), ia.isSystem()))
            .collect(toSet()))
        .orElse(Set.of());
    return !before.attributes().equals(currentAttrs);
  }
}
