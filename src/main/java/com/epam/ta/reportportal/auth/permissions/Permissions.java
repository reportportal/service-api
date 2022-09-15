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

	public static final String ASSIGNED_TO_PROJECT = "hasPermission(#projectKey.toLowerCase(), 'isAssignedToProject')";

	public static final String PROJECT_MANAGER = "hasPermission(#projectKey.toLowerCase(), 'projectManagerPermission')";

	public static final String PROJECT_MANAGER_OR_ADMIN =
			"hasPermission(#projectKey.toLowerCase(), 'projectManagerPermission')" + "||" + ADMIN_ONLY;
}