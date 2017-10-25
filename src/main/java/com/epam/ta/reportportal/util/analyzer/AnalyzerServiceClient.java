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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

/**
 * Simple HTTP client for log indexing/analysis service.
 *
 * @author Ivan Sharamet
 */
@Service("analyzerServiceClient")
public class AnalyzerServiceClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerServiceClient.class);

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
		List<ServiceInstance> analyzerInstances = getAnalyzerServiceInstances();
		return analyzerInstances.stream()
				.filter(instance -> Boolean.valueOf(instance.getMetadata().get(DOES_NEED_INDEX)))
				.map(instance -> index(instance, rq))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
	}

	public IndexLaunch analyze(IndexLaunch rq) {
		List<ServiceInstance> analyzerInstances = getAnalyzerServiceInstances();
		analyzerInstances.sort(comparingLong((ServiceInstance it) -> Long.parseLong(it.getMetadata().get(PRIORITY))).reversed());
		for (ServiceInstance instance : analyzerInstances) {
			Optional<IndexLaunch> analyzed = analyze(instance, rq);
			if (analyzed.isPresent()) {
				rq = analyzed.get();
			}
		}
		return rq;
	}

	/**
	 * Indexes test items logs by specified analyzer instance.
	 *
	 * @param analyzer Analyzer
	 * @param rq       Request {@link List<IndexLaunch>} to index
	 * @return {@link Optional} of {@link IndexRs} with indexed items
	 */
	private Optional<IndexRs> index(ServiceInstance analyzer, List<IndexLaunch> rq) {
		try {
			ResponseEntity<IndexRs> responseEntity = restTemplate.postForEntity(
					analyzer.getUri().toString() + INDEX_PATH, rq, IndexRs.class);
			return Optional.ofNullable(responseEntity.getBody());
		} catch (Exception e) {
			LOGGER.warn("Indexing failed. Cannot interact with {} analyzer. Error: {}", analyzer.getMetadata().get(ANALYZER_KEY), e);
		}
		return Optional.empty();
	}

	/**
	 * Analyze test items by specified analyzer instance.
	 *
	 * @param analyzer Analyzer
	 * @param rq       Request {@link IndexLaunch} to analyze
	 * @return {@link Optional} of {@link IndexLaunch} with analyzed items
	 */
	private Optional<IndexLaunch> analyze(ServiceInstance analyzer, IndexLaunch rq) {
		try {
			ResponseEntity<IndexLaunch[]> responseEntity = restTemplate.postForEntity(
					analyzer.getUri().toString() + ANALYZE_PATH, Collections.singletonList(rq), IndexLaunch[].class);
			IndexLaunch[] rs = responseEntity.getBody();
			if (rs.length > 0) {
				return Optional.ofNullable(rs[0]);
			}
		} catch (Exception e) {
			LOGGER.warn("Analyzing failed. Cannot interact with {} analyzer. Error: {}", analyzer.getMetadata().get(ANALYZER_KEY), e);
		}
		return Optional.empty();
	}

	/**
	 * Get list of available analyzers instances
	 *
	 * @return {@link List} of instances
	 */
	private List<ServiceInstance> getAnalyzerServiceInstances() {
		return discoveryClient.getServices()
				.stream()
				.flatMap(service -> discoveryClient.getInstances(service).stream())
				.filter(instance -> instance.getMetadata().containsKey(ANALYZER_KEY))
				.collect(toList());
	}
}
