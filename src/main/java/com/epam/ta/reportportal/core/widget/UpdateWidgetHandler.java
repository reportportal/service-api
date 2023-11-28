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

package com.epam.ta.reportportal.core.widget;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import java.util.Collection;

/**
 * @author Pavel Bortnik
 */
public interface UpdateWidgetHandler {

  /**
   * Update a widget with a specified id.
   *
   * @param widgetId The ID of the widget to be updated
   * @param updateRQ The {@link WidgetRQ} containing the updated information for the widget
   * @param projectDetails The {@link ReportPortalUser.ProjectDetails} for the project associated with the widget
   * @param user The {@link ReportPortalUser} who is updating the widget
   * @return An {@link OperationCompletionRS} instance indicating the result of the update operation
   */
  OperationCompletionRS updateWidget(Long widgetId, WidgetRQ updateRQ,
      ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user);

}