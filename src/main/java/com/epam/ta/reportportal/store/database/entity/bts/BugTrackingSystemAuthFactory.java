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

package com.epam.ta.reportportal.store.database.entity.bts;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.database.entity.bts.auth.ApiKeyAuth;
import com.epam.ta.reportportal.store.database.entity.bts.auth.BasicAuth;
import com.epam.ta.reportportal.store.database.entity.bts.auth.NtlmAuth;
import com.epam.ta.reportportal.store.database.entity.enums.AuthType;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.CreateExternalSystemRQ;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Pavel Bortnik
 */
@Component
public class BugTrackingSystemAuthFactory {

	@Autowired
	private BasicTextEncryptor simpleEncryptor;

	public BugTrackingSystemAuth createAuthObject(AuthType authType, CreateExternalSystemRQ rq) {
		switch (authType) {
			case APIKEY:
				ApiKeyAuth apiKeyAuth = new ApiKeyAuth();
				apiKeyAuth.setAuthType(authType);
				apiKeyAuth.setAccessKey(rq.getAccessKey());
				return apiKeyAuth;
			case BASIC:
				BasicAuth basicAuth = new BasicAuth();
				basicAuth.setAuthType(authType);
				basicAuth.setUsername(rq.getUsername());
				basicAuth.setPassword(simpleEncryptor.encrypt(rq.getPassword()));
				return basicAuth;
			case NTLM:
				NtlmAuth auth = new NtlmAuth();
				auth.setUsername(rq.getUsername());
				auth.setPassword(simpleEncryptor.encrypt(rq.getPassword()));
				auth.setDomain(rq.getDomain());
				return auth;
			default:
				throw new ReportPortalException(ErrorType.INCORRECT_AUTHENTICATION_TYPE);
		}
	}

}
