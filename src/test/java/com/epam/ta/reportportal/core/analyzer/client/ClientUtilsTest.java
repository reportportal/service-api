/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.client;

import com.google.common.collect.ImmutableMap;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.epam.ta.reportportal.core.analyzer.client.ClientUtils.ANALYZER_INDEX;
import static com.epam.ta.reportportal.core.analyzer.client.ClientUtils.ANALYZER_PRIORITY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Pavel Bortnik
 */
public class ClientUtilsTest {

	@Test
	public void testParsing() {
		ExchangeInfo mock = mock(ExchangeInfo.class);
		when(mock.getArguments()).thenReturn(ImmutableMap.<String, Object>builder().put(ANALYZER_PRIORITY, 1)
				.put(ANALYZER_INDEX, true)
				.build());
		Assert.assertEquals(1, ClientUtils.EXCHANGE_PRIORITY.applyAsInt(mock));
		Assert.assertTrue(ClientUtils.DOES_SUPPORT_INDEX.test(mock));
	}

	@Test
	public void testDefaultValues() {
		ExchangeInfo mock = mock(ExchangeInfo.class);
		when(mock.getArguments()).thenReturn(Collections.emptyMap());
		Assert.assertEquals(Integer.MAX_VALUE, ClientUtils.EXCHANGE_PRIORITY.applyAsInt(mock));
		Assert.assertFalse(ClientUtils.DOES_SUPPORT_INDEX.test(mock));
	}

	@Test
	public void testBadValues() {
		ExchangeInfo mock = mock(ExchangeInfo.class);
		when(mock.getArguments()).thenReturn(ImmutableMap.<String, Object>builder().put(ANALYZER_PRIORITY, "abracadabra")
				.put(ANALYZER_INDEX, "666")
				.build());
		Assert.assertEquals(Integer.MAX_VALUE, ClientUtils.EXCHANGE_PRIORITY.applyAsInt(mock));
		Assert.assertFalse(ClientUtils.DOES_SUPPORT_INDEX.test(mock));
	}

}