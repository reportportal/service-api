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

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.util.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.util.analyzer.model.IndexRs;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * HTTP client for all log indexing/analysis services. Such services are
 * those that have tag {@link AnalyzerServiceClient#ANALYZER_KEY} in
 * service's metadata.
 * <p>
 * To define that service indexes data before analyzing that it should
 * be indicated by tag {@link AnalyzerServiceClient#DOES_NEED_INDEX}
 * with <code>true</code> in metadata.
 * <p>
 * Items are analyzed in order of priority specified in tag
 * {@link AnalyzerServiceClient#PRIORITY} in metadata. If several analyzers
 * provided different issues for one item, it would be overwritten with
 * results of more priority service.
 *
 * @author Ivan Sharamet
 * @author Pavel Bortnik
 */
@Service("analyzerServiceClient")
public class AnalyzerServiceClient {

	private static final String INDEX_PATH = "/_index";
	private static final String ANALYZE_PATH = "/_analyze";

	private static final String ANALYZER_KEY = "analyzer";
	private static final String PRIORITY = "analyzer_priority";
	private static final String DOES_NEED_INDEX = "analyzer_index";

	private final RestTemplate restTemplate;
	private final DiscoveryClient discoveryClient;

	@Autowired
	public AnalyzerServiceClient(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
		this.restTemplate = restTemplate;
		this.discoveryClient = discoveryClient;
	}

	public void checkAccess() {
		BusinessRule.expect(getAnalyzerServiceInstances().isEmpty(), Predicate.isEqual(false))
				.verify(ErrorType.UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM, "There are no analyzer services are deployed.");
	}

	public List<IndexRs> index(List<IndexLaunch> rq) {
		List<String> urls = getAnalyzerServiceInstances().stream()
				.filter(it -> Boolean.valueOf(it.getMetadata().get(DOES_NEED_INDEX)))
				.map(it -> it.getUri().toString())
				.collect(toList());
		return urls.stream()
				.map(serviceUrl -> restTemplate.postForEntity(serviceUrl + INDEX_PATH, rq, IndexRs.class))
				.map(HttpEntity::getBody)
				.collect(toList());
	}

	public IndexLaunch analyze(IndexLaunch rq) {
		List<ServiceInstance> analyzerInstances = getAnalyzerServiceInstances();
		analyzerInstances.sort(Comparator.comparingLong((ServiceInstance it) -> Long.parseLong(it.getMetadata().get(PRIORITY))).reversed());
		for (ServiceInstance instance : analyzerInstances) {
			ResponseEntity<IndexLaunch[]> responseEntity = restTemplate.postForEntity(
					instance.getUri().toString() + ANALYZE_PATH, Collections.singletonList(rq), IndexLaunch[].class);
			rq = responseEntity.getBody()[0];
		}
		return rq;
	}

	private List<ServiceInstance> getAnalyzerServiceInstances() {
		return discoveryClient.getServices()
				.stream()
				.flatMap(service -> discoveryClient.getInstances(service).stream())
				.filter(instance -> instance.getMetadata().containsKey(ANALYZER_KEY))
				.collect(toList());
	}
}
