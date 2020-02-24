/*
 * Copyright 2020 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.migration;

import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.ws.converter.builders.IntegrationBuilder;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class AbstractSecretMigrationServiceTest {

	@Test
	void nullParamsTest() {
		Optional<Map<String, Object>> optional = AbstractSecretMigrationService.extractParams(new Integration());
		assertTrue(optional.isEmpty());
	}

	@Test
	void emptyParamsTest() {
		Optional<Map<String, Object>> optional = AbstractSecretMigrationService.extractParams(new IntegrationBuilder().withParams(new IntegrationParams(
				null)).get());
		assertTrue(optional.isPresent());
		assertTrue(optional.get().isEmpty());
	}

	@Test
	void extractParamsPositive() {
		final HashMap<String, Object> params = Maps.newHashMap();
		params.put("1", "1");
		params.put("2", "2");
		final Optional<Map<String, Object>> optional = AbstractSecretMigrationService.extractParams(new IntegrationBuilder().withParams(new IntegrationParams(
				params)).get());
		assertTrue(optional.isPresent());
		assertFalse(optional.get().isEmpty());
		assertEquals(2, optional.get().size());
	}
}