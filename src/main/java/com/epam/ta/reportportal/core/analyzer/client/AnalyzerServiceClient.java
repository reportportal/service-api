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

import com.epam.ta.reportportal.core.analyzer.IAnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexRs;
import com.epam.ta.reportportal.events.ConsulUpdateEvent;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.epam.ta.reportportal.core.analyzer.client.ClientUtils.*;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

@Service
public class AnalyzerServiceClient implements IAnalyzerServiceClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerServiceClient.class);

	static final String INDEX_PATH = "/_index";
	static final String ANALYZE_PATH = "/_analyze";

	private final RestTemplate restTemplate;
	private final DiscoveryClient discoveryClient;

	private List<ServiceInstance> analyzerInstances;

	@Autowired
	public AnalyzerServiceClient(RestTemplate restTemplate, DiscoveryClient consulDiscoveryClient) {
		this.restTemplate = restTemplate;
		this.discoveryClient = consulDiscoveryClient;
		getAnalyzerServiceInstances(null);
	}

	@Override
	public boolean hasClients() {
		return !analyzerInstances.isEmpty();
	}

	@Override
	public List<IndexRs> index(List<IndexLaunch> rq) {
		return analyzerInstances.stream()
				.filter(DOES_NEED_INDEX)
				.map(instance -> index(instance, rq))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
	}

	//Make services return only updated items and refactor this
	@Override
	public IndexLaunch analyze(IndexLaunch rq) {
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
			LOGGER.warn("Analyzing failed. Cannot interact with {} analyzer.", analyzer.getMetadata().get(ANALYZER_KEY), e);
		}
		return Optional.empty();
	}

	/**
	 * Get list of available analyzers instances
	 *
	 * @return {@link List} of instances
	 */
	@EventListener
	@VisibleForTesting
	private void getAnalyzerServiceInstances(ConsulUpdateEvent event) {
		analyzerInstances = new CopyOnWriteArrayList<>();
		discoveryClient.getServices()
				.stream()
				.flatMap(service -> discoveryClient.getInstances(service).stream())
				.filter(instance -> instance.getMetadata().containsKey(ANALYZER_KEY))
				.sorted(comparingInt(SERVICE_PRIORITY).reversed())
				.forEach(it -> analyzerInstances.add(it));
	}
}
