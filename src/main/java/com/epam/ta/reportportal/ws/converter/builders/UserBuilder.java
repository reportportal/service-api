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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.entity.user.UserType;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQConfirm;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
@Scope("prototype")
public class UserBuilder extends Builder<User> {

	private static final HashFunction HASH_FUNCTION = Hashing.md5();

	public UserBuilder addCreateUserRQ(CreateUserRQConfirm request) {
		if (request != null) {
			User user = getObject();
			user.setLogin(EntityUtils.normalizeId(request.getLogin()));
			user.setPassword(HASH_FUNCTION.hashString(request.getPassword(), Charsets.UTF_8).toString());
			user.setEmail(EntityUtils.normalizeId(request.getEmail().trim()));
			user.setDefaultProject(EntityUtils.normalizeId(request.getDefaultProject()));
			user.setFullName(request.getFullName());
			user.setType(UserType.INTERNAL);
			user.setIsExpired(false);
			user.getMetaInfo().setLastLogin(Calendar.getInstance().getTime());
		}
		return this;
	}

	public UserBuilder addUserRole(UserRole userRole) {
		getObject().setRole(userRole);
		return this;
	}

	@Override
	protected User initObject() {
		return new User();
	}
}
