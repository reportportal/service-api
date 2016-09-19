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

package com.epam.ta.reportportal.core.acl;

import java.util.Optional;
import java.util.Set;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.database.entity.sharing.Acl;
import com.epam.ta.reportportal.database.entity.sharing.AclEntry;
import com.epam.ta.reportportal.database.entity.sharing.AclPermissions;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import com.epam.ta.reportportal.ws.model.ErrorType;

/**
 * Provide utilities for sharing and monopolizing {@link Shareable} objects.
 * 
 * @author Aliaksei_Makayed
 * 
 */
public class AclUtils {

	private AclUtils() {
	}

	public static void modifyACL(Acl acl, String projectName, String userModifier, boolean isShare) {
		if (!acl.getOwnerUserId().equals(userModifier)) {
			// Impossible to share/unshare not owned resources
			return;
		}
		if (isShare) {
			share(acl, projectName);
		} else {
			monopolize(acl, projectName);
		}
	}

	public static void monopolize(Acl acl, String projectName) {
		Set<AclEntry> entries = acl.getEntries();
		Optional<AclEntry> aclEntry = entries.stream().filter(Preconditions.hasACLPermission(projectName, AclPermissions.READ)).findFirst();
		if (aclEntry.isPresent()) {
			if (aclEntry.get().getPermissions().size() > 1) {
				aclEntry.get().removePermission(AclPermissions.READ);
			} else {
				entries.remove(aclEntry.get());
			}
		}
	}

	// TODO consider move this validation to permission validation layer
	public static void validateOwner(Acl acl, String userModifier, String resourceName) {
		BusinessRule.expect(acl, Preconditions.isOwner(userModifier)).verify(ErrorType.UNABLE_MODIFY_SHARABLE_RESOURCE,
				Suppliers.formattedSupplier("User {} isn't owner of {} resource.", userModifier, resourceName));
	}

	/**
	 * Validate is specified acl owned by specified user or is shared to
	 * specified project
	 * 
	 * @param acl
	 * @param userModifier
	 * @param projectName
	 */
	@SuppressWarnings("unchecked")
	public static void isPossibleToRead(Acl acl, String userModifier, String projectName) {
		BusinessRule.expect(acl, Predicates.or(Preconditions.isOwner(userModifier), Preconditions.isSharedTo(projectName)))
				.verify(ErrorType.UNABLE_MODIFY_SHARABLE_RESOURCE, Suppliers.formattedSupplier(
						"User '{}' isn't owner of resource and resource isn't shared to project '{}'.", userModifier, projectName));
	}

	/**
	 * Validate is specified acl owned by specified user or is shared to
	 * specified project but don't rise exception
	 * 
	 * @param acl
	 * @param userModifier
	 * @param projectName
	 */
	public static boolean isPossibleToReadResource(Acl acl, String userModifier, String projectName) {
		// Not resource owner
		if (!acl.getOwnerUserId().equalsIgnoreCase(userModifier))
			if (!acl.getEntries().stream().filter(Preconditions.hasACLPermission(projectName, AclPermissions.READ)).findFirst().isPresent())
				return false;
		return true;
	}

	public static void share(Acl acl, String projectName) {
		Set<AclEntry> entries = acl.getEntries();
		Optional<AclEntry> entry = entries.stream().filter(Preconditions.hasACLPermission(projectName, null)).findFirst();
		if (entry.isPresent()) {
			entry.get().addPermission(AclPermissions.READ);
		} else {
			AclEntry aclEntry = new AclEntry();
			aclEntry.setProjectId(projectName);
			aclEntry.addPermission(AclPermissions.READ);
			entries.add(aclEntry);
		}
	}
}