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