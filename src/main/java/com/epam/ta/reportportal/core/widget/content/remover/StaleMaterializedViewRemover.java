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

import com.epam.ta.reportportal.core.widget.content.materialized.generator.MaterializedViewNameGenerator;
import com.epam.ta.reportportal.dao.StaleMaterializedViewRepository;
import com.epam.ta.reportportal.entity.materialized.StaleMaterializedView;
import com.epam.ta.reportportal.entity.widget.Widget;
import java.time.Instant;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class StaleMaterializedViewRemover implements WidgetContentRemover {

  private final MaterializedViewNameGenerator materializedViewNameGenerator;
  private final StaleMaterializedViewRepository staleMaterializedViewRepository;

  public StaleMaterializedViewRemover(MaterializedViewNameGenerator materializedViewNameGenerator,
      StaleMaterializedViewRepository staleMaterializedViewRepository) {
    this.materializedViewNameGenerator = materializedViewNameGenerator;
    this.staleMaterializedViewRepository = staleMaterializedViewRepository;
  }

  @Override
  public void removeContent(Widget widget) {
    final StaleMaterializedView staleView = getStaleView(widget);
    staleMaterializedViewRepository.insert(staleView);
  }

  private StaleMaterializedView getStaleView(Widget widget) {
    final String viewName = materializedViewNameGenerator.generate(widget);
    final StaleMaterializedView view = new StaleMaterializedView();
    view.setName(viewName);
    view.setCreationDate(Instant.now());
    return view;
  }
}
