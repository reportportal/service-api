/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public final class LinkGenerator {

	private static final String UI_PREFIX = "/ui/#";
	private static final String LAUNCHES = "/launches/all/";

	private LinkGenerator() {
		//static only
	}

	public static String generateLaunchLink(String baseUrl, String projectName, String id) {
		return StringUtils.isEmpty(baseUrl) ? null : baseUrl + UI_PREFIX + projectName + LAUNCHES + id;
	}

	public static String composeBaseUrl(HttpServletRequest request) {
		/*
		 * Use Uri components since they are aware of x-forwarded-host headers
		 */
		return UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request))
				.replacePath(null)
				.replaceQuery(null)
				.build()
				.toUri()
				.toASCIIString();
	}
}
