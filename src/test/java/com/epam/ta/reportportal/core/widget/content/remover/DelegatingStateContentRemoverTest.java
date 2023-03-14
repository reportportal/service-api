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

package com.epam.ta.reportportal.core.widget.content.remover;

import static com.epam.ta.reportportal.entity.widget.WidgetType.COMPONENT_HEALTH_CHECK;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.widget.content.materialized.state.WidgetStateResolver;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class DelegatingStateContentRemoverTest {

  private final WidgetStateResolver widgetStateResolver = mock(WidgetStateResolver.class);

  private final WidgetContentRemover remover = mock(WidgetContentRemover.class);
  private final Map<WidgetState, WidgetContentRemover> removerMapping = Map.of(
      WidgetState.RENDERING, remover);

  private final DelegatingStateContentRemover delegatingStateContentRemover = new DelegatingStateContentRemover(
      widgetStateResolver,
      removerMapping
  );

  @ParameterizedTest
  @ValueSource(strings = {"cumulative", "componentHealthCheckTable"})
  void supports(String type) {
    final Widget widget = new Widget();
    widget.setWidgetType(type);

    when(widgetStateResolver.resolve(widget.getWidgetOptions())).thenReturn(WidgetState.RENDERING);

    delegatingStateContentRemover.removeContent(widget);
    verify(remover, times(1)).removeContent(widget);
  }

  @Test
  void supportsHealthNegative() {
    final Widget widget = new Widget();
    widget.setWidgetType(COMPONENT_HEALTH_CHECK.getType());
    delegatingStateContentRemover.removeContent(widget);
    verify(widgetStateResolver, times(0)).resolve(any(WidgetOptions.class));
  }
}