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

package com.epam.reportportal.base.infrastructure.persistence.entity.enums.converter;

import com.epam.reportportal.base.infrastructure.persistence.entity.AnalyzeMode;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * @author Ivan Budayeu
 */
@Converter(autoApply = true)
public class AnalyzerModeConverter implements AttributeConverter<AnalyzeMode, String> {

  @Override
  public String convertToDatabaseColumn(AnalyzeMode attribute) {
    return attribute.getValue();
  }

  @Override
  public AnalyzeMode convertToEntityAttribute(String dbAnalyzerModeName) {
    return AnalyzeMode.fromString(dbAnalyzerModeName)
        .orElseThrow(
            () -> new ReportPortalException("Can not convert user type name from database."));
  }
}
