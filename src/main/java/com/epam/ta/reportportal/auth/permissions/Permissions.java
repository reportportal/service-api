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

package com.epam.ta.reportportal.auth.permissions;

/**
 * Set of constants related to permissions
 *
 * @author Andrei Varabyeu
 */
public final class Permissions {

	private Permissions() {
		// constants holder
	}

	public static final String ALLOWED_TO_EDIT_USER = "(#login.toLowerCase() == authentication.name) || hasRole('ADMINISTRATOR')";

	public static final String ADMIN_ONLY = "hasRole('ADMINISTRATOR')";

	public static final String ALLOWED_TO_REPORT = "hasPermission(#projectName.toLowerCase(), 'reporterPermission')";

	public static final String PROJECT_MANAGER = "hasPermission(#projectName.toLowerCase(), 'projectManagerPermission')";

	public static final String NOT_CUSTOMER = "hasPermission(#projectName.toLowerCase(), 'notCustomerPermission')";

	public static final String PROJECT_MANAGER_OR_ADMIN =
			"hasPermission(#projectName.toLowerCase(), 'projectManagerPermission')" + "||" + ADMIN_ONLY;

	public static final String ASSIGNED_TO_PROJECT = "hasPermission(#projectName.toLowerCase(), 'isAssignedToProject')";
}