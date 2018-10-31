/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.widget;

import com.epam.ta.reportportal.entity.widget.Widget;

public interface ShareWidgetHandler {

    /**
     * Share widget to project
     *
     * @param projectName
     * @param widgetId
     * @return list of all shared widgets for the project
     */
    void shareWidget(String projectName, Long widgetId);

    /**
     * Find widget
     * @param widgetId
     * @return widget
     */
    Widget findById(Long widgetId);
}
