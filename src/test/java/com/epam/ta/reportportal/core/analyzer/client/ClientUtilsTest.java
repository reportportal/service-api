/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.analyzer.client;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;

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
		ServiceInstance mock = mock(ServiceInstance.class);
		when(mock.getMetadata()).thenReturn(
				ImmutableMap.<String, String>builder().put(ANALYZER_PRIORITY, "1").put(ANALYZER_INDEX, "true").build());
		Assert.assertEquals(1, ClientUtils.SERVICE_PRIORITY.applyAsInt(mock));
		Assert.assertEquals(true, ClientUtils.SUPPORT_INDEX.test(mock));
	}

	@Test
	public void testDefaultValues() {
		ServiceInstance mock = mock(ServiceInstance.class);
		when(mock.getMetadata()).thenReturn(Collections.emptyMap());
		Assert.assertEquals(Integer.MAX_VALUE, ClientUtils.SERVICE_PRIORITY.applyAsInt(mock));
		Assert.assertEquals(false, ClientUtils.SUPPORT_INDEX.test(mock));
	}

	@Test
	public void testBadValues() {
		ServiceInstance mock = mock(ServiceInstance.class);
		when(mock.getMetadata()).thenReturn(
				ImmutableMap.<String, String>builder().put(ANALYZER_PRIORITY, "abracadabra").put(ANALYZER_INDEX, "666").build());
		Assert.assertEquals(Integer.MAX_VALUE, ClientUtils.SERVICE_PRIORITY.applyAsInt(mock));
		Assert.assertEquals(false, ClientUtils.SUPPORT_INDEX.test(mock));
	}

}