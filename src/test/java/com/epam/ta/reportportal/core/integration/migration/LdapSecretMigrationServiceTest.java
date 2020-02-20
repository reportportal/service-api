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

import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.ws.converter.builders.IntegrationBuilder;
import com.google.common.collect.Maps;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class LdapSecretMigrationServiceTest {

	@Mock
	private IntegrationRepository integrationRepository;

	@Mock
	private BasicTextEncryptor encryptor;

	@InjectMocks
	private LdapSecretMigrationService migrationService;

	private static BasicTextEncryptor staticSaltEncryptor;

	@BeforeEach
	void before() {
		ReflectionTestUtils.setField(migrationService, "salt", "reportportal");
		staticSaltEncryptor = new BasicTextEncryptor();
		staticSaltEncryptor.setPassword("reportportal");
	}

	@Test
	void emptyIntegrationListTest() {
		when(integrationRepository.findAllByTypeIn("ldap")).thenReturn(Collections.emptyList());
		migrationService.migrate();
		verify(encryptor, never()).encrypt(anyString());
	}

	@Test
	void integrationWithoutPasswordParameterTest() {
		Integration integration = testIntegration(BtsProperties.URL.getName(), "url");
		when(integrationRepository.findAllByTypeIn("ldap")).thenReturn(List.of(integration));
		migrationService.migrate();
		verify(encryptor, never()).encrypt(anyString());
		assertTrue(Objects.isNull(integration.getParams().getParams().get("managerPassword")));
	}

	@Test
	void passwordShouldBeEncrypted() {
		String unencrypted = "unencrypted";
		String encrypted = "new-encrypted-pass";
		Integration integration = testIntegration("managerPassword", staticSaltEncryptor.encrypt(unencrypted));
		when(integrationRepository.findAllByTypeIn("ldap")).thenReturn(List.of(integration));
		when(encryptor.encrypt(unencrypted)).thenReturn(encrypted);
		migrationService.migrate();
		verify(encryptor, times(1)).encrypt(unencrypted);
		Optional<String> parameterOptional = Optional.ofNullable(integration.getParams().getParams().get("managerPassword"))
				.map(it -> (String) it);
		assertTrue(parameterOptional.isPresent());
		assertEquals(encrypted, parameterOptional.get());
	}

	private Integration testIntegration(String key, String value) {
		final HashMap<String, Object> params = Maps.newHashMap();
		params.put(key, value);
		return new IntegrationBuilder().withParams(new IntegrationParams(params)).get();
	}

}