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

package com.epam.ta.reportportal.core.user.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.epam.ta.reportportal.database.entity.user.UserRole;

/**
 * Update user role event handler based on Spring's {@link ApplicationListener}
 * 
 * @author Andrei_Ramanchuk
 *
 */
@Component
public class UpdateUserRoleEventHandler implements ApplicationListener<UpdateUserRoleEvent> {

	//TODO fix!
//	@Autowired
	//private AccessTokenService tokenService;

	@Override
	public void onApplicationEvent(UpdateUserRoleEvent event) {
		String username = event.getUpdatedRole().getUsername();
		UserRole role = event.getUpdatedRole().getUserRole();
		//tokenService.rememberRoleUpdate(username, role);
	}
}