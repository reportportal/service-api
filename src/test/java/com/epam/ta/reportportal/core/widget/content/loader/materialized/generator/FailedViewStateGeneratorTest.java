/*
 * Copyright 2022 EPAM Systems
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

package com.epam.ta.reportportal.core.widget.content.loader.materialized.generator;

import static com.epam.ta.reportportal.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;
import org.springframework.util.LinkedMultiValueMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class FailedViewStateGeneratorTest {

  private final ViewGenerator delegate = mock(ViewGenerator.class);
  private final WidgetRepository widgetRepository = mock(WidgetRepository.class);

  private final FailedViewStateGenerator generator = new FailedViewStateGenerator(delegate,
      widgetRepository);

  @Test
  void shouldCatchExceptionAndSetFailedState() {

    final boolean refresh = false;
    final String viewName = "viewName";
    final Widget widget = getWidget();
    Sort sort = Sort.unsorted();
    final LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();

    Filter filter = Filter.builder().withTarget(Widget.class)
        .withCondition(FilterCondition.builder().eq("id", "1").build()).build();
    doThrow(RuntimeException.class).when(delegate)
        .generate(refresh, viewName, widget, filter, sort, params);

    generator.generate(refresh, viewName, widget, filter, sort, params);

    final ArgumentCaptor<Widget> widgetArgumentCaptor = ArgumentCaptor.forClass(Widget.class);
    verify(widgetRepository, times(1)).save(widgetArgumentCaptor.capture());

    final Widget failedWidget = widgetArgumentCaptor.getValue();
    final String failedState = WidgetOptionUtil.getValueByKey(STATE,
        failedWidget.getWidgetOptions());

    Assertions.assertEquals(WidgetState.FAILED.getValue(), failedState);

  }

  private Widget getWidget() {
    Widget widget = new Widget();
    widget.setId(1L);
    widget.setWidgetType("componentHealthCheckTable");
    return new WidgetBuilder(widget).addProject(1L).addOption(STATE, WidgetState.CREATED.getValue())
        .get();
  }
}