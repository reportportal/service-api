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

package com.epam.ta.reportportal.core.launch.util;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public final class LaunchLinkGenerator {

	private static final String UI_PREFIX = "/ui/#";
	private static final String LAUNCHES = "/launches/all/";

	private LaunchLinkGenerator() {
		//static only
	}

	public static String generateLaunchLink(LinkParams linkParams, String id) {
		return linkParams.getBaseUrl() + id;
	}

	public static class LinkParams {
		private String baseUrl;

		private LinkParams(String scheme, String host, String projectName) {
			this.baseUrl = scheme + "://" + host + UI_PREFIX + projectName + LAUNCHES;
		}

		public LinkParams() {
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public static LinkParams of(String scheme, String host, String projectName) {
			return new LinkParams(scheme, host, projectName);
		}
	}
}
