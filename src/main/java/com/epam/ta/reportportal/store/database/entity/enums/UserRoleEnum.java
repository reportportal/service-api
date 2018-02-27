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

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * UserRole representation<br>
 * Role has more rights than the following one. So, Administrator is more
 * privileged than User.
 *
 * @author Andrei Varabyeu
 */
public enum UserRoleEnum {

	USER,
	ADMINISTRATOR;

	public static final String ROLE_PREFIX = "ROLE_";

	public static Optional<UserRoleEnum> findByName(String name) {
		return Arrays.stream(UserRoleEnum.values()).filter(role -> role.name().equals(name)).findAny();
	}

	public static Optional<UserRoleEnum> findByAuthority(String name) {
		if (Strings.isNullOrEmpty(name)) {
			return Optional.empty();
		}
		return findByName(StringUtils.substringAfter(name, ROLE_PREFIX));
	}

	public String getAuthority() {
		return "ROLE_" + this.name();
	}

}