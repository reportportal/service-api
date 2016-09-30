/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import java.util.Calendar;

import com.epam.ta.reportportal.database.entity.user.UserType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.entity.user.UserUtils;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQConfirm;

@Service
@Scope("prototype")
public class UserBuilder extends Builder<User> {

	public UserBuilder addCreateUserRQ(CreateUserRQConfirm request) {
		if (request != null) {
			User user = getObject();
			user.setLogin(EntityUtils.normalizeUsername(request.getLogin()));
			user.setPassword(UserUtils.generateMD5(request.getPassword()));
			user.setEmail(EntityUtils.normalizeEmail(request.getEmail().trim()));
			user.setDefaultProject(EntityUtils.normalizeProjectName(request.getDefaultProject()));
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
