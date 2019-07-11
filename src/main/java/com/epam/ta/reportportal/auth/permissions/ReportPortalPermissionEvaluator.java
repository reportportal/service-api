/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.auth.permissions;

import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.PermissionNotDefinedException;
import com.google.common.base.Preconditions;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Map;

/**
 * ReportPortal permission evaluator
 *
 * @author Andrei Varabyeu
 */
// TODO add custom exception handling
class ReportPortalPermissionEvaluator implements PermissionEvaluator {

	private static final GrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority(UserRole.ADMINISTRATOR.getAuthority());

	/**
	 * Mapping between permission names and permissions
	 */
	private Map<String, Permission> permissionNameToPermissionMap;

	private boolean allowAllToAdmin;

	public ReportPortalPermissionEvaluator(Map<String, Permission> permissionNameToPermissionMap) {
		this(permissionNameToPermissionMap, true);

	}

	public ReportPortalPermissionEvaluator(Map<String, Permission> permissionNameToPermissionMap, boolean allowAllToAdmin) {
		this.permissionNameToPermissionMap = Preconditions.checkNotNull(permissionNameToPermissionMap);
		this.allowAllToAdmin = allowAllToAdmin;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		boolean hasPermission = false;
		if (canHandle(authentication, targetDomainObject, permission)) {
			hasPermission = checkPermission(authentication, targetDomainObject, (String) permission);
		}
		return hasPermission;
	}

	private boolean canHandle(Authentication authentication, Object targetDomainObject, Object permission) {
		return targetDomainObject != null && authentication != null && String.class.equals(permission.getClass());
	}

	private boolean checkPermission(Authentication authentication, Object targetDomainObject, String permissionKey) {
		verifyPermissionIsDefined(permissionKey);
		if (allowAllToAdmin && authentication.isAuthenticated() && authentication.getAuthorities().contains(ADMIN_AUTHORITY)) {
			return true;
		}
		Permission permission = permissionNameToPermissionMap.get(permissionKey);
		return permission.isAllowed(authentication, targetDomainObject);
	}

	private void verifyPermissionIsDefined(String permissionKey) {
		if (!permissionNameToPermissionMap.containsKey(permissionKey)) {
			throw new PermissionNotDefinedException(
					"No permission with key " + permissionKey + " is defined in " + this.getClass().toString());
		}
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
		throw new PermissionNotDefinedException("Id and Class permissions are not supperted by " + this.getClass().toString());
	}
}