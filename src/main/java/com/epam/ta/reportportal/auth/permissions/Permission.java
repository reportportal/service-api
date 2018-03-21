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

import org.springframework.security.core.Authentication;

/**
 * Report Portal Permission representation<br>
 * <b>BE AWARE</b> that each permissions should be marked with
 * {@link com.epam.ta.reportportal.auth.permissions.LookupPermission} annotation
 * to be assigned to some permission name. Without this permission will be
 * ignored by {@link org.springframework.security.access.PermissionEvaluator}
 *
 * @author Andrei Varabyeu
 */
public interface Permission {

	/**
	 * Is action allowed for user with {@link Authentication} for target object
	 *
	 * @param authentication
	 * @param targetDomainObject
	 * @return
	 */
	boolean isAllowed(Authentication authentication, Object targetDomainObject);
}