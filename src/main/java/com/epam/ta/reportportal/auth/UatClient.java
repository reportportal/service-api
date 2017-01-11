/*
 * Copyright 2016 EPAM Systems
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
