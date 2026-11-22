/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.marketplace;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PluginArtifactsTest {

  @Test
  void versionMayCarryBuildMetadataButIdentifierMayNot() {
    assertTrue(PluginArtifacts.isSafeVersion("1.0.0+build.5"));
    assertTrue(PluginArtifacts.isSafeVersion("5.11.0-SNAPSHOT"));
    assertFalse(PluginArtifacts.isSafeIdentifier("1.0.0+build.5"));
  }

  @Test
  void versionRejectsSeparatorsAndDotSegments() {
    assertFalse(PluginArtifacts.isSafeVersion(null));
    assertFalse(PluginArtifacts.isSafeVersion(""));
    assertFalse(PluginArtifacts.isSafeVersion("../1.0"));
    assertFalse(PluginArtifacts.isSafeVersion("1.0/2"));
    assertFalse(PluginArtifacts.isSafeVersion("+1.0"));
    assertFalse(PluginArtifacts.isSafeVersion("1.0 2"));
  }
}
