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

import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.epam.ta.reportportal.entity.widget.WidgetType.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class MaterializedViewContentRemoverTest {

	private final WidgetContentRepository widgetContentRepository = mock(WidgetContentRepository.class);

	private MaterializedViewContentRemover materializedViewContentRemover;


	@BeforeEach
	public void setUp() {
		materializedViewContentRemover = new MaterializedViewContentRemover(widgetContentRepository);
	}

	@Test
	void supportsCumulative() {
		final Widget widget = new Widget();
		widget.setWidgetType(CUMULATIVE.getType());
		assertTrue(materializedViewContentRemover.supports(widget));
	}

	@Test
	void supportsHealthCheckTable() {
		final Widget widget = new Widget();
		widget.setWidgetType(COMPONENT_HEALTH_CHECK_TABLE.getType());
		assertTrue(materializedViewContentRemover.supports(widget));
	}

	@Test
	void supportsHealthNegative() {
		final Widget widget = new Widget();
		widget.setWidgetType(COMPONENT_HEALTH_CHECK.getType());
		assertFalse(materializedViewContentRemover.supports(widget));
	}
}