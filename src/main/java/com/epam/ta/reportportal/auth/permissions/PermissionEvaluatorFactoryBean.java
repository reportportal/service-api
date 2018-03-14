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

import com.epam.ta.reportportal.util.ApplicationContextAwareFactoryBean;
import org.springframework.security.access.PermissionEvaluator;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Factory bean for providing permissions marked with {@link LookupPermission}
 * annotation
 *
 * @author Andrei Varabyeu
 */
public class PermissionEvaluatorFactoryBean extends ApplicationContextAwareFactoryBean<PermissionEvaluator> {

	@Override
	public Class<?> getObjectType() {
		return PermissionEvaluator.class;
	}

	@Override
	protected PermissionEvaluator createInstance() {

		/*
		 * Find all beans in context marked with
		 * com.epam.ta.reportportal.auth.permissions.LookupPermission annotation
		 */
		Map<String, Object> permissionBeans = getApplicationContext().getBeansWithAnnotation(LookupPermission.class);
		Map<String, Permission> permissionsMap = new HashMap<>();
		for (Entry<String, Object> permission : permissionBeans.entrySet()) {
			/*
			 * There will be no NPE since we asked bean factory to get beans
			 * with this annotation
			 */
			for (String permissionName : permission.getValue().getClass().getAnnotation(LookupPermission.class).value()) {
				if (Permission.class.isAssignableFrom(permission.getValue().getClass())) {
					/*
					 * Assign permission name from LookupPermission annotation
					 * to it's value
					 */
					permissionsMap.put(permissionName, (Permission) permission.getValue());
				}
			}
		}

		return new ReportPortalPermissionEvaluator(permissionsMap);
	}
}