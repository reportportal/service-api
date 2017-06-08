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

import java.util.UUID;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.database.entity.user.UserCreationBid;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQ;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;

/**
 * New user creation bid builder
 * 
 * @author Andrei_Ramanchuk
 */
@Service
@Scope("prototype")
public class UserCreationBidBuilder extends Builder<UserCreationBid> {

	public UserCreationBidBuilder addUserCreationBid(CreateUserRQ request) {
		if (request != null) {
			getObject().setId(UUID.randomUUID().toString());
			getObject().setEmail(normalizeId(request.getEmail().trim()));
			getObject().setDefaultProject(normalizeId(request.getDefaultProject()));
			getObject().setRole(request.getRole());
		}
		return this;
	}

	@Override
	protected UserCreationBid initObject() {
		return new UserCreationBid();
	}
}
