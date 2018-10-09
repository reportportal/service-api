/*
 * Copyright 2018 EPAM Systems
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
package com.epam.ta.reportportal.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */

@Component
public class InfoContributorComposite implements InfoContributor {

	private static final String EXTENSIONS_KEY = "extensions";

	private final List<ExtensionContributor> infoContributors;

	@Autowired
	public InfoContributorComposite(List<ExtensionContributor> infoContributors) {
		this.infoContributors = infoContributors;
	}

	@Override
	public void contribute(Info.Builder builder) {
		builder.withDetail(EXTENSIONS_KEY, infoContributors.stream()
				.map(ExtensionContributor::contribute)
				.flatMap(map -> map.entrySet().stream())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}
}
