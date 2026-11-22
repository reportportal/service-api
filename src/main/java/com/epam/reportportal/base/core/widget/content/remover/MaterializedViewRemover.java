/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.widget.content.remover;

import com.epam.reportportal.base.core.widget.content.materialized.generator.MaterializedViewNameGenerator;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetContentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Removes the materialized view associated with a widget when the widget is deleted.
 *
 * <p>The view name is always derived from the widget's project and id, never taken from the
 * stored widget options, so a widget can only drop its own view.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class MaterializedViewRemover implements WidgetContentRemover {

  private final WidgetContentRepository widgetContentRepository;
  private final MaterializedViewNameGenerator materializedViewNameGenerator;

  @Autowired
  public MaterializedViewRemover(WidgetContentRepository widgetContentRepository,
      MaterializedViewNameGenerator materializedViewNameGenerator) {
    this.widgetContentRepository = widgetContentRepository;
    this.materializedViewNameGenerator = materializedViewNameGenerator;
  }

  @Override
  public void removeContent(Widget widget) {
    widgetContentRepository.removeWidgetView(materializedViewNameGenerator.generate(widget));
  }
}
