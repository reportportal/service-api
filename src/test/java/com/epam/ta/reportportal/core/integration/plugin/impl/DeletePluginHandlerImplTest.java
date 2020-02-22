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

package com.epam.ta.reportportal.core.integration.plugin.impl;

import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.ReservedIntegrationTypeEnum;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DeletePluginHandlerImplTest {

	@Mock
	private IntegrationTypeRepository integrationTypeRepository;

	@InjectMocks
	private DeletePluginHandlerImpl deletePluginHandler;

	@Test
	void deleteReservedIntegrationTypesTest() {
		Arrays.stream(ReservedIntegrationTypeEnum.values()).map(ReservedIntegrationTypeEnum::getName).forEach(it -> {
			when(integrationTypeRepository.findById(1L)).thenReturn(Optional.of(testIntegrationType(it)));

			ReportPortalException exception = assertThrows(ReportPortalException.class, () -> deletePluginHandler.deleteById(1L));
			assertEquals(String.format("Error during plugin removing: 'Unable to remove reserved plugin - '%s''", it),
					exception.getMessage()
			);
		});
	}

	private IntegrationType testIntegrationType(String name) {
		IntegrationType integrationType = new IntegrationType();
		integrationType.setId(1L);
		integrationType.setName(name);
		return integrationType;
	}
}