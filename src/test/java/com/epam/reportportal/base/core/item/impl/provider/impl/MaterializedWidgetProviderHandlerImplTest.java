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

package com.epam.reportportal.base.core.item.impl.provider.impl;

import static com.epam.reportportal.base.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static com.epam.reportportal.base.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.item.impl.provider.DataProviderHandler;
import com.epam.reportportal.base.core.widget.content.materialized.generator.MaterializedViewNameGenerator;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetOptions;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

class MaterializedWidgetProviderHandlerImplTest {

  private static final Long PROJECT_ID = 3L;
  private static final Long WIDGET_ID = 10L;

  private final WidgetRepository widgetRepository = mock(WidgetRepository.class);
  private final DataProviderHandler cumulativeProvider = mock(DataProviderHandler.class);
  private final MaterializedWidgetProviderHandlerImpl handler =
      new MaterializedWidgetProviderHandlerImpl();
  private final MembershipDetails membershipDetails =
      MembershipDetails.builder().withProjectId(PROJECT_ID).build();
  private final Queryable filter = mock(Queryable.class);
  private final Pageable pageable = PageRequest.of(0, 10);

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(handler, "widgetRepository", widgetRepository);
    ReflectionTestUtils.setField(handler, "materializedViewNameGenerator",
        new MaterializedViewNameGenerator());
    ReflectionTestUtils.setField(handler, "testItemWidgetDataProviders",
        Map.of(WidgetType.CUMULATIVE, cumulativeProvider));
  }

  @Test
  void passesGeneratedViewNameWhenStoredNameMatches() {
    mockWidget("widget_3_10");
    Map<String, String> params = params();

    handler.getTestItems(filter, pageable, membershipDetails, null, params);

    verify(cumulativeProvider).getTestItems(eq(filter), eq(pageable), eq(membershipDetails),
        any(), eq(params));
    assertEquals("widget_3_10", params.get(VIEW_NAME));
  }

  @Test
  void requestViewNameParameterIsOverwritten() {
    mockWidget("widget_3_10");
    Map<String, String> params = params();
    params.put(VIEW_NAME, "widget_4_1");

    handler.accumulateStatistics(filter, membershipDetails, null, params);

    assertEquals("widget_3_10", params.get(VIEW_NAME));
  }

  @ParameterizedTest
  @ValueSource(strings = {"widget_4_10", "widget_3_11", "launch",
      "widget_3_10; DROP TABLE launch"})
  void foreignOrMalformedStoredViewNameIsNotUsed(String storedName) {
    mockWidget(storedName);

    ReportPortalException items = assertThrows(ReportPortalException.class,
        () -> handler.getTestItems(filter, pageable, membershipDetails, null, params()));
    ReportPortalException statistics = assertThrows(ReportPortalException.class,
        () -> handler.accumulateStatistics(filter, membershipDetails, null, params()));

    assertEquals(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, items.getErrorType());
    assertEquals(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, statistics.getErrorType());
    verifyNoInteractions(cumulativeProvider);
  }

  @Test
  void missingStoredViewNameIsRejected() {
    mockWidget(null);

    assertThrows(ReportPortalException.class,
        () -> handler.getTestItems(filter, pageable, membershipDetails, null, params()));
    verifyNoInteractions(cumulativeProvider);
  }

  @Test
  void unsupportedWidgetTypeIsRejected() {
    Widget widget = mockWidget("widget_3_10");
    widget.setWidgetType(WidgetType.COMPONENT_HEALTH_CHECK_TABLE.getType());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.getTestItems(filter, pageable, membershipDetails, null, params()));
    assertEquals(ErrorType.BAD_REQUEST_ERROR, exception.getErrorType());
  }

  private Widget mockWidget(String storedViewName) {
    Widget widget = new Widget();
    widget.setId(WIDGET_ID);
    widget.setWidgetType(WidgetType.CUMULATIVE.getType());
    Project project = new Project();
    project.setId(PROJECT_ID);
    widget.setProject(project);
    Map<String, Object> options = new HashMap<>();
    options.put(STATE, "ready");
    if (storedViewName != null) {
      options.put(VIEW_NAME, storedViewName);
    }
    widget.setWidgetOptions(new WidgetOptions(options));
    when(widgetRepository.findByIdAndProjectId(WIDGET_ID, PROJECT_ID))
        .thenReturn(Optional.of(widget));
    return widget;
  }

  private static Map<String, String> params() {
    Map<String, String> params = new HashMap<>();
    params.put("widgetId", String.valueOf(WIDGET_ID));
    return params;
  }
}
