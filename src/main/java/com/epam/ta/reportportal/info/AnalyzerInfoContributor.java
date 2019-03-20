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

package com.epam.ta.reportportal.info;

import com.epam.ta.reportportal.core.analyzer.client.RabbitMqManagementClient;
import com.google.common.collect.ImmutableMap;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.analyzer.client.impl.AnalyzerUtils.ANALYZER_KEY;

/**
 * Shows list of supported analyzers
 *
 * @author Pavel Bortnik
 */
@Component
public class AnalyzerInfoContributor implements ExtensionContributor {

	private final RabbitMqManagementClient managementClient;

	@Autowired
	public AnalyzerInfoContributor(RabbitMqManagementClient managementClient) {
		this.managementClient = managementClient;
	}

	@Override
	public Map<String, ?> contribute() {
		Set<String> names = managementClient.getAnalyzerExchangesInfo().stream().map(ExchangeInfo::getName).collect(Collectors.toSet());
		return ImmutableMap.<String, Object>builder().put(ANALYZER_KEY, names).build();
	}
}
