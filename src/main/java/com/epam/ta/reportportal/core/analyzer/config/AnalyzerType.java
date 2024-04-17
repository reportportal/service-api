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

package com.epam.ta.reportportal.core.analyzer.config;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.rules.exception.ErrorType;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public enum AnalyzerType {

  AUTO_ANALYZER("autoAnalyzer"),
  PATTERN_ANALYZER("patternAnalyzer");

  private final String name;

  AnalyzerType(String name) {
    this.name = name;
  }

  public static AnalyzerType fromString(String type) {
    return Arrays.stream(AnalyzerType.values())
        .filter(it -> it.getName().equalsIgnoreCase(type))
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.INCORRECT_REQUEST,
            "Incorrect analyzer type. Allowed are: " + Arrays.stream(AnalyzerType.values())
                .map(AnalyzerType::getName)
                .collect(Collectors.toList())
        ));
  }

  public String getName() {
    return name;
  }
}
