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

package com.epam.ta.reportportal.util.analyzer;

import com.epam.ta.reportportal.util.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.util.analyzer.model.IndexRs;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Tests for {@link AnalyzerServiceClient}
 *
 * @author Ivan Sharamet
 */
public class AnalyzerServiceClientTest {

	private static final String SERVICE_URL = "http://analyzer";

	private RestTemplate restTemplate;
	private DiscoveryClient discoveryClient;
	private AnalyzerServiceClient client;

	@Before
	public void setup() {
		restTemplate = Mockito.mock(RestTemplate.class);
		discoveryClient = Mockito.mock(DiscoveryClient.class);
		client = new AnalyzerServiceClient(restTemplate,discoveryClient);
	}

	@Test
	public void testIndex() {
		IndexRs expectedRs = new IndexRs();
		responseWith("/_index", expectedRs);
		IndexRs actualRs = client.index(Collections.singletonList(new IndexLaunch()));
		Assert.assertSame(expectedRs, actualRs);
	}

	@Test
	public void testAnalyze() {
		IndexLaunch expectedRs = new IndexLaunch();
		responseWith("/_analyze", new IndexLaunch[] { expectedRs });
		IndexLaunch actualRs = client.analyze(new IndexLaunch());
		Assert.assertSame(expectedRs, actualRs);
	}

	@Test
	public void testAnalyzeWithEmptyRs() {
		responseWith("/_analyze", new IndexLaunch[] {});
		IndexLaunch rs = client.analyze(new IndexLaunch());
		Assert.assertNull(rs);
	}

	@SuppressWarnings("unchecked")
	private void responseWith(String path, Object rs) {
		Mockito.when(restTemplate.postForEntity(Mockito.eq(SERVICE_URL + path), Mockito.anyListOf(IndexLaunch.class), Mockito.any()))
				.thenReturn(new ResponseEntity(rs, HttpStatus.OK));
	}
}
