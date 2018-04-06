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
import com.epam.ta.reportportal.store.database.entity.enums.AuthType;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.CreateExternalSystemRQ;
import com.google.common.base.Preconditions;
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

	public BugTrackingSystemAuth createAuthObject(BugTrackingSystemAuth auth, CreateExternalSystemRQ rq) {
		Preconditions.checkNotNull(rq, "Provided parameter can't be null");
		AuthType authType = AuthType.findByName(rq.getExternalSystemAuth());
		auth = resetFields(auth);
		auth.setAuthType(authType);
		switch (authType) {
			case APIKEY:
				if (null != rq.getAccessKey()) {
					auth.setAccessKey(rq.getAccessKey());
					return auth;
				}
				break;
			case BASIC:
				if (null != rq.getUsername() && null != rq.getPassword()) {
					auth.setUsername(rq.getUsername());
					auth.setPassword(simpleEncryptor.encrypt(rq.getPassword()));
					return auth;
				}
				break;
			case NTLM:
				if (null != rq.getUsername() && null != rq.getPassword() && null != rq.getDomain()) {
					auth.setUsername(rq.getUsername());
					auth.setPassword(simpleEncryptor.encrypt(rq.getPassword()));
					auth.setDomain(rq.getDomain());
					return auth;
				}
				break;
			case OAUTH:
				if (null != rq.getAccessKey()) {
					auth.setAccessKey(rq.getAccessKey());
					return auth;
				}
				break;
			default:
				throw new ReportPortalException(ErrorType.INCORRECT_AUTHENTICATION_TYPE);
		}
		throw new ReportPortalException(ErrorType.INCORRECT_REQUEST);
	}

	private BugTrackingSystemAuth resetFields(BugTrackingSystemAuth auth) {
		auth.setAccessKey(null);
		auth.setUsername(null);
		auth.setDomain(null);
		auth.setPassword(null);
		return auth;
	}
}
