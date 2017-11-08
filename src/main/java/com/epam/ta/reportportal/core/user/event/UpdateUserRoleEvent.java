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

package com.epam.ta.reportportal.core.user.event;

import org.springframework.context.ApplicationEvent;

/**
 * Update User role event for handling situations with already logged-in
 * accounts using tokens.
 *
 * @author Andrei_Ramanchuk
 */
public class UpdateUserRoleEvent extends ApplicationEvent {

	private static final long serialVersionUID = -1379108969230583507L;

	public UpdateUserRoleEvent(UpdatedRole source) {
		super(source);
	}

	public UpdatedRole getUpdatedRole() {
		return (UpdatedRole) super.getSource();
	}
}