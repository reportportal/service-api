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

package com.epam.ta.reportportal.core.widget.content.remover;

import static com.epam.ta.reportportal.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class MaterializedViewRemoverTest {

  private final WidgetContentRepository widgetContentRepository = mock(
      WidgetContentRepository.class);
  private final MaterializedViewRemover materializedViewRemover = new MaterializedViewRemover(
      widgetContentRepository);

  @Test
  void shouldRemove() {
    final Widget widget = new Widget();
    final String viewName = "name";
    widget.setWidgetOptions(new WidgetOptions(Map.of(VIEW_NAME, viewName)));

    materializedViewRemover.removeContent(widget);

    verify(widgetContentRepository, times(1)).removeWidgetView(viewName);
  }

}