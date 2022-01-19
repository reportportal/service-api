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

import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class MaterializedViewRemover implements WidgetContentRemover {

	private final WidgetContentRepository widgetContentRepository;

	@Autowired
	public MaterializedViewRemover(WidgetContentRepository widgetContentRepository) {
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	public void removeContent(Widget widget) {
		ofNullable(WidgetOptionUtil.getValueByKey(VIEW_NAME,
				widget.getWidgetOptions()
		)).ifPresent(widgetContentRepository::removeWidgetView);
	}
}
