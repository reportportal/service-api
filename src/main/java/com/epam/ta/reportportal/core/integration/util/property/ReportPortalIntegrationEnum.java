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

package com.epam.ta.reportportal.core.integration.util.property;

import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public enum ReportPortalIntegrationEnum {

	JIRA(IntegrationGroupEnum.BTS, true),
	RALLY(IntegrationGroupEnum.BTS, true),
	EMAIL(IntegrationGroupEnum.NOTIFICATION, false),
	SAUCELABS(IntegrationGroupEnum.NOTIFICATION, true);

	private IntegrationGroupEnum integrationGroup;
	private boolean isPlugin;

	ReportPortalIntegrationEnum(IntegrationGroupEnum integrationGroup, boolean isPlugin) {
		this.integrationGroup = integrationGroup;
		this.isPlugin = isPlugin;
	}

	public IntegrationGroupEnum getIntegrationGroup() {
		return integrationGroup;
	}

	public boolean isPlugin() {
		return isPlugin;
	}

	public static Optional<ReportPortalIntegrationEnum> findByName(String name) {
		return Arrays.stream(ReportPortalIntegrationEnum.values())
				.filter(integration -> integration.name().equalsIgnoreCase(name))
				.findFirst();
	}
}
