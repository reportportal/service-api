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

package com.epam.reportportal.extension.common;

import com.epam.reportportal.extension.AuthExtension;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

/**
 * Enumeration with all available extension points.
 *
 * @author Andrei Varabyeu
 */
@Getter
public enum ExtensionPoint {

  BTS(BtsExtension.class),
  AUTH(AuthExtension.class),
  REPORT_PORTAL(ReportPortalExtensionPoint.class);

  private final Class<? extends org.pf4j.ExtensionPoint> extensionClass;

  ExtensionPoint(Class<? extends org.pf4j.ExtensionPoint> extension) {
    this.extensionClass = extension;
  }

  public static Optional<ExtensionPoint> findByExtension(Class<?> clazz) {
    return Arrays.stream(values())
        .filter(it -> it.extensionClass.isAssignableFrom(clazz)).findAny();
  }
}
