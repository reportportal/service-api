/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.extension.common;


import com.epam.reportportal.base.extension.common.ExtensionPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExtensionPointTest {

  @Test
  public void findByExtensionPositive() {
    Assertions.assertTrue(
        ExtensionPoint.findByExtension(ExtensionPoint.BTS.getExtensionClass()).isPresent(),
        "Incorrect find by extension");
  }

  @Test
  public void findByExtensionNegative() {
    Assertions.assertFalse(
        ExtensionPoint.findByExtension(String.class).isPresent(),
        "Incorrect find by extension");
  }
}
