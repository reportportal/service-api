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

import com.epam.ta.reportportal.database.entity.user.RestorePasswordBid;
import com.epam.ta.reportportal.ws.model.user.RestorePasswordRQ;

/**
 * @author Dzmitry_Kavalets
 */
@Service
@Scope("prototype")
public class RestorePasswordBidBuilder extends Builder<RestorePasswordBid> {

	public RestorePasswordBidBuilder addRestorePasswordBid(RestorePasswordRQ rq) {
		getObject().setEmail(rq.getEmail());
		getObject().setId(UUID.randomUUID().toString());
		return this;
	}

	@Override
	protected RestorePasswordBid initObject() {
		return new RestorePasswordBid();
	}
}