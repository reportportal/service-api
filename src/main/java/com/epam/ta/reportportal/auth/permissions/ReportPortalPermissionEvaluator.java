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

import com.epam.ta.reportportal.database.entity.user.UserRole;
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