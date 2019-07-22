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

package com.epam.ta.reportportal.core.integration.util.property;

import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class BtsPropertiesTest {

	@Test
	void setParam() {
		IntegrationParams params = new IntegrationParams();
		BtsProperties.USER_NAME.setParam(params, "value");

		assertTrue(params.getParams().containsKey(BtsProperties.USER_NAME.getName()));
		assertEquals("value", params.getParams().get(BtsProperties.USER_NAME.getName()));
	}

	@Test
	void getEmptyName() {
		assertFalse(BtsProperties.PROJECT.getParam(Collections.emptyMap()).isPresent());
	}
}