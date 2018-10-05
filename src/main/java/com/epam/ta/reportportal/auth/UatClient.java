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

package com.epam.ta.reportportal.auth;

import com.google.common.base.Preconditions;
import org.springframework.web.client.RestTemplate;

/**
 * UAT service client. UAT service in charge of all auth-related stuff such as handling of access tokens
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public class UatClient {

	private final RestTemplate restTemplate;
	private final String uatServiceUrl;

	public UatClient(String uatServiceUrl, RestTemplate restTemplate) {
		this.restTemplate = Preconditions.checkNotNull(restTemplate, "RestTemplate should not be null");
		this.uatServiceUrl = Preconditions.checkNotNull(uatServiceUrl, "UAT service URL should not be null");
	}

	public void revokeUserTokens(String user) {
		this.restTemplate.delete(uatServiceUrl + "/sso/internal/user/{user}", user);
	}
}
