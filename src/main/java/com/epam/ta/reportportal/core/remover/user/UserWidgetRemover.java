/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.remover.user;

import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.core.widget.content.remover.WidgetContentRemover;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class UserWidgetRemover implements ContentRemover<User> {

  private final WidgetRepository widgetRepository;
  private final WidgetContentRemover widgetContentRemover;

  public UserWidgetRemover(WidgetRepository widgetRepository,
      @Qualifier("delegatingStateContentRemover") WidgetContentRemover widgetContentRemover) {
    this.widgetRepository = widgetRepository;
    this.widgetContentRemover = widgetContentRemover;
  }

  @Override
  public void remove(User user) {
    List<Widget> widgets = widgetRepository.findAllByOwnerAndWidgetTypeIn(user.getLogin(),
        Collections.singletonList(WidgetType.COMPONENT_HEALTH_CHECK_TABLE.getType())
    );
    widgets.forEach(widgetContentRemover::removeContent);
  }
}
