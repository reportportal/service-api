/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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