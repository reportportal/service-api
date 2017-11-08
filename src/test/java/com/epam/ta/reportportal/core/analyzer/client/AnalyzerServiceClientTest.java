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

import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexLog;
import com.epam.ta.reportportal.core.analyzer.model.IndexRs;
import com.epam.ta.reportportal.core.analyzer.model.IndexTestItem;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

import static com.epam.ta.reportportal.core.analyzer.client.AnalyzerServiceClient.ANALYZE_PATH;
import static com.epam.ta.reportportal.core.analyzer.client.AnalyzerServiceClient.INDEX_PATH;
import static com.epam.ta.reportportal.core.analyzer.client.ClientUtils.*;
import static com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AnalyzerServiceClient}
 *
 * @author Ivan Sharamet
 */
@SuppressWarnings("unchecked")
public class AnalyzerServiceClientTest {

	private static final URI SERVICE_URL = URI.create("http://analyzer");

	private RestTemplate restTemplate;
	private DiscoveryClient discoveryClient;
	private AnalyzerServiceClient client;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setup() {
		restTemplate = Mockito.mock(RestTemplate.class);
		discoveryClient = Mockito.mock(DiscoveryClient.class);
		client = new AnalyzerServiceClient(restTemplate, discoveryClient);
	}

	@Test
	public void noClientsAvailable() {
		IndexLaunch rq = new IndexLaunch();
		when(discoveryClient.getServices()).thenReturn(Collections.emptyList());
		List<IndexRs> actualRs = client.index(Collections.singletonList(rq));
		boolean b = client.hasClients();
		IndexLaunch rs = client.analyze(rq);
		Assert.assertFalse("Should be false if no services", b);
		Assert.assertTrue("Response should be empty is there are no services.", actualRs.isEmpty());
		Assert.assertEquals("Request and response should be equals", rq, rs);
	}

	@Test
	public void testIndex() {
		List<IndexLaunch> rq = Collections.singletonList(indexLaunch());

		ServiceInstance mock = mock(ServiceInstance.class);
		when(discoveryClient.getServices()).thenReturn(ImmutableList.<String>builder().add("service").build());
		when(discoveryClient.getInstances("service")).thenReturn(Collections.singletonList(mock));
		when(mock.getMetadata()).thenReturn(
				ImmutableMap.<String, String>builder().put(ANALYZER_INDEX, "true").put(ANALYZER_KEY, "ml").build());
		when(mock.getUri()).thenReturn(SERVICE_URL);

		IndexRs index = new IndexRs();
		index.setErrors(false);

		when(restTemplate.postForEntity(SERVICE_URL + INDEX_PATH, rq, IndexRs.class)).thenReturn(new ResponseEntity(index, HttpStatus.OK));

		List<IndexRs> actualRs = client.index(rq);
		Assert.assertSame(index.isErrors(), actualRs.get(0).isErrors());
	}

	@Test
	public void indexUnsupportedService() {
		ServiceInstance mock = mock(ServiceInstance.class);
		when(discoveryClient.getServices()).thenReturn(ImmutableList.<String>builder().add("service").build());
		when(discoveryClient.getInstances("service")).thenReturn(Collections.singletonList(mock));
		when(mock.getMetadata()).thenReturn(
				ImmutableMap.<String, String>builder().put(ANALYZER_INDEX, "false").put(ANALYZER_KEY, "ml").build());
		when(mock.getUri()).thenReturn(SERVICE_URL);
		IndexLaunch rq = new IndexLaunch();

		List<IndexRs> index = client.index(Collections.singletonList(rq));
		Assert.assertTrue("Should be empty", index.isEmpty());
	}

	@Test
	public void testIndexException() {
		IndexLaunch rq = indexLaunch();
		ServiceInstance mock = mock(ServiceInstance.class);
		when(discoveryClient.getServices()).thenReturn(ImmutableList.<String>builder().add("service").build());
		when(discoveryClient.getInstances("service")).thenReturn(Collections.singletonList(mock));
		when(mock.getMetadata()).thenReturn(
				ImmutableMap.<String, String>builder().put(ANALYZER_INDEX, "false").put(ANALYZER_KEY, "ml").build());
		when(mock.getUri()).thenReturn(SERVICE_URL);

		List<IndexRs> index = client.index(Collections.singletonList(rq));
		Assert.assertTrue(index.isEmpty());
	}

	private void analyzerPreconditions() {
		ServiceInstance mock = mock(ServiceInstance.class);
		when(discoveryClient.getServices()).thenReturn(ImmutableList.<String>builder().add("service").build());
		when(discoveryClient.getInstances("service")).thenReturn(Collections.singletonList(mock));
		when(mock.getMetadata()).thenReturn(ImmutableMap.<String, String>builder().put(ANALYZER_INDEX, "true")
				.put(ANALYZER_KEY, "ml")
				.put(ANALYZER_PRIORITY, "1")
				.build());
		when(mock.getUri()).thenReturn(SERVICE_URL);
	}

	@Test
	public void testAnalyze() {
		IndexLaunch rq = indexLaunch();

		analyzerPreconditions();
		when(restTemplate.postForEntity(SERVICE_URL + ANALYZE_PATH, Collections.singletonList(rq), IndexLaunch[].class)).thenReturn(
				new ResponseEntity<>(basicInvestigation(), HttpStatus.OK));

		IndexLaunch rs = client.analyze(rq);
		rs.getTestItems().forEach(it -> Assert.assertEquals(it.getIssueType(), PRODUCT_BUG.getLocator()));
	}

	@Test
	public void testAnalyzeException() {
		IndexLaunch rq = indexLaunch();
		analyzerPreconditions();
		responseAnalyzeException(rq);
		IndexLaunch rs = client.analyze(rq);
		Assert.assertSame(rq, rs);
		Assert.assertEquals(rs, indexLaunch());
	}

	private void responseAnalyzeException(IndexLaunch rq) {
		when(restTemplate.postForEntity(SERVICE_URL + ANALYZE_PATH, Collections.singletonList(rq), IndexLaunch[].class)).thenThrow(
				new NumberFormatException());
	}

	@Test
	public void testAnalyzerPriority() {
		ServiceInstance topPriority = mock(ServiceInstance.class);
		ServiceInstance second = mock(ServiceInstance.class);
		when(discoveryClient.getServices()).thenReturn(ImmutableList.<String>builder().add("service").build());
		when(discoveryClient.getInstances("service")).thenReturn(Lists.newArrayList(topPriority, second));

		when(topPriority.getMetadata()).thenReturn(ImmutableMap.<String, String>builder().put(ANALYZER_INDEX, "true")
				.put(ANALYZER_KEY, "highest priority")
				.put(ANALYZER_PRIORITY, "0")
				.build());

		when(second.getMetadata()).thenReturn(ImmutableMap.<String, String>builder().put(ANALYZER_INDEX, "true")
				.put(ANALYZER_KEY, "ml")
				.put(ANALYZER_PRIORITY, "1")
				.build());

		when(second.getUri()).thenReturn(SERVICE_URL);
		when(topPriority.getUri()).thenReturn(URI.create("PRIORITY"));

		IndexLaunch rq = indexLaunch();

		responseLowPriorityService(rq);
		responseTopPriorityService(basicInvestigation()[0]);

		IndexLaunch analyze = client.analyze(rq);

		Assert.assertEquals(SYSTEM_ISSUE.getLocator(), analyze.getTestItems().get(0).getIssueType());
		Assert.assertEquals(PRODUCT_BUG.getLocator(), analyze.getTestItems().get(1).getIssueType());

	}

	private void responseTopPriorityService(IndexLaunch rq) {
		IndexLaunch[] launches = new IndexLaunch[1];
		IndexLaunch analyzed = indexLaunch();
		analyzed.getTestItems().get(0).setIssueType(SYSTEM_ISSUE.getLocator());
		analyzed.getTestItems().get(1).setIssueType(PRODUCT_BUG.getLocator());
		launches[0] = analyzed;
		when(restTemplate.postForEntity("PRIORITY" + ANALYZE_PATH, Collections.singletonList(rq), IndexLaunch[].class)).thenReturn(
				new ResponseEntity<>(launches, HttpStatus.OK));
	}

	private void responseLowPriorityService(IndexLaunch rq) {

		when(restTemplate.postForEntity(SERVICE_URL + ANALYZE_PATH, Collections.singletonList(rq), IndexLaunch[].class)).thenReturn(
				new ResponseEntity<>(basicInvestigation(), HttpStatus.OK));
	}

	private IndexLaunch[] basicInvestigation() {
		IndexLaunch[] launches = new IndexLaunch[1];
		IndexLaunch analyzed = indexLaunch();
		analyzed.getTestItems().forEach(it -> it.setIssueType(PRODUCT_BUG.getLocator()));
		launches[0] = analyzed;
		return launches;
	}

	private IndexLaunch indexLaunch() {
		IndexLaunch indexLaunch = new IndexLaunch();
		indexLaunch.setLaunchId("indexLaunch");
		indexLaunch.setLaunchName("launch");
		indexLaunch.setProject("project");
		indexLaunch.setTestItems(testItemsTI());
		return indexLaunch;
	}

	private List<IndexTestItem> testItemsTI() {
		List<IndexTestItem> list = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			IndexTestItem testItem = new IndexTestItem();
			testItem.setIssueType(TO_INVESTIGATE.getLocator());
			testItem.setLogs(errorLogs());
			list.add(testItem);
		}
		return list;
	}

	private Set<IndexLog> errorLogs() {
		Set<IndexLog> set = new HashSet<>(2);
		for (int i = 0; i < 2; i++) {
			IndexLog indexLog = new IndexLog();
			indexLog.setLogLevel(LogLevel.ERROR_INT);
			indexLog.setMessage("Error message " + i);
			set.add(indexLog);
		}
		return set;
	}

}
