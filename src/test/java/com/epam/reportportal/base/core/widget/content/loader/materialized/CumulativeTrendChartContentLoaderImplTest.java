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

package com.epam.reportportal.base.core.widget.content.loader.materialized;

import static com.epam.reportportal.base.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTES;
import static com.epam.reportportal.base.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.widget.content.materialized.generator.MaterializedViewNameGenerator;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetContentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetOptions;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;

class CumulativeTrendChartContentLoaderImplTest {

  private final WidgetContentRepository widgetContentRepository =
      mock(WidgetContentRepository.class);
  private final CumulativeTrendChartContentLoaderImpl loader =
      new CumulativeTrendChartContentLoaderImpl();

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(loader, "widgetContentRepository", widgetContentRepository);
    ReflectionTestUtils.setField(loader, "materializedViewNameGenerator",
        new MaterializedViewNameGenerator());
  }

  @Test
  void usesOwnViewName() {
    when(widgetContentRepository.cumulativeTrendChart(anyString(), anyString(), any(), any()))
        .thenReturn(Collections.emptyList());

    loader.loadContent(widget("widget_1_2"), new LinkedMultiValueMap<>());

    verify(widgetContentRepository).cumulativeTrendChart(
        org.mockito.ArgumentMatchers.eq("widget_1_2"),
        org.mockito.ArgumentMatchers.eq("build"), isNull(), isNull());
  }

  @Test
  void foreignViewNameIsNotUsed() {
    LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.put(ATTRIBUTES, List.of("build:1"));

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> loader.loadContent(widget("widget_9_2"), params));

    assertEquals(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, exception.getErrorType());
    verifyNoInteractions(widgetContentRepository);
  }

  private static Widget widget(String storedViewName) {
    Widget widget = new Widget();
    widget.setId(2L);
    Project project = new Project();
    project.setId(1L);
    widget.setProject(project);
    Map<String, Object> options = new HashMap<>();
    options.put(VIEW_NAME, storedViewName);
    options.put(ATTRIBUTES, List.of("build"));
    widget.setWidgetOptions(new WidgetOptions(options));
    return widget;
  }
}
