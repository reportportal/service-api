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

package com.epam.ta.reportportal.store.database.entity.enums;

import com.epam.ta.reportportal.exception.ReportPortalException;

import java.util.Arrays;

import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_AUTHENTICATION_TYPE;

/**
 * Authentication mechanics enum for external system
 *
 * @author Andrei_Ramanchuk
 */
public enum AuthType {

	//@formatter:off
	OAUTH(false),
	NTLM(true),
	APIKEY(true),
	BASIC(true);
	//@formatter:on

	final boolean requiresPassword;

	AuthType(boolean requiresPassword) {
		this.requiresPassword = requiresPassword;
	}

	public boolean requiresPassword() {
		return requiresPassword;
	}

	public static AuthType findByName(String name) {
		return Arrays.stream(AuthType.values())
				.filter(type -> type.name().equalsIgnoreCase(name))
				.findAny()
				.orElseThrow(() -> new ReportPortalException(INCORRECT_AUTHENTICATION_TYPE, name));
	}

	public static boolean isPresent(String name) {
		return null != findByName(name);
	}
}