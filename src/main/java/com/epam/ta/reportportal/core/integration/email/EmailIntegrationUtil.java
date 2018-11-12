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

package com.epam.ta.reportportal.core.integration.email;

import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.email.SendCaseType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.entity.project.email.SendCaseType.LAUNCH_STATS_RULE;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class EmailIntegrationUtil {

	public static final String EMAIL = "email";
	private static final String RULES = "rules";

	private EmailIntegrationUtil() {
		//static only
	}

	/**
	 * Extract email integration from project
	 *
	 * @param project Project
	 * @return Optional of Integration
	 */
	public static Optional<Integration> getEmailIntegration(Project project) {
		if (project != null && CollectionUtils.isNotEmpty(project.getIntegrations())) {
			return project.getIntegrations().stream().filter(it -> it.getType().getName().equalsIgnoreCase(EMAIL)).findFirst();
		}
		return Optional.empty();
	}

	/**
	 * Extract email rules from email integration parameters
	 *
	 * @param integrationParams Email integration parameters
	 * @return List of rules
	 */
	public static List<Map<String, Object>> getEmailRules(Map<String, Object> integrationParams) {
		if (integrationParams != null) {
			return Optional.ofNullable((List<Map<String, Object>>) integrationParams.get(RULES)).orElse(Collections.emptyList());
		}
		return Collections.emptyList();
	}

	/**
	 * Get list of case values
	 *
	 * @param rule     Rule to extract
	 * @param caseType Case to extract
	 * @return List of case values
	 */
	public static List<String> getRuleValues(Map<String, Object> rule, SendCaseType caseType) {
		if (caseType != null && caseType != SendCaseType.LAUNCH_STATS_RULE) {
			return Optional.ofNullable((List<String>) rule.get(caseType.getCaseTypeString())).orElse(Collections.emptyList());
		}
		return Collections.emptyList();
	}

	public static String getLaunchStatsValue(Map<String, Object> rule) {
		if (MapUtils.isNotEmpty(rule)) {
			return (String) rule.get(LAUNCH_STATS_RULE.getCaseTypeString());
		}
		return null;
	}

}
