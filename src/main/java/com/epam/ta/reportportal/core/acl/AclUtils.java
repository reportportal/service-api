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

package com.epam.ta.reportportal.core.acl;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.sharing.Acl;
import com.epam.ta.reportportal.database.entity.sharing.AclEntry;
import com.epam.ta.reportportal.database.entity.sharing.AclPermissions;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.or;

/**
 * Provide utilities for sharing and monopolizing {@link Shareable} objects.
 *
 * @author Aliaksei_Makayed
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
	public static void isAllowedToEdit(Acl acl, String userModifier, Map<String, ProjectRole> userProjects, String resourceName,
			UserRole userRole) {
		if (!UserRole.ADMINISTRATOR.equals(userRole)) {
			BusinessRule.expect(acl, Preconditions.isOwner(userModifier).or(hasProjectRole(userProjects, ProjectRole.PROJECT_MANAGER)))
					.verify(ErrorType.UNABLE_MODIFY_SHARABLE_RESOURCE,
							Suppliers.formattedSupplier("User {} isn't owner of {} resource.", userModifier, resourceName)
					);
		}
	}

	/**
	 * Checks all cases before deleting specified widget from database.
	 * Widget deleted if modifier is the widget's owner, if admin or project manager
	 * deletes it not from own dashboard.
	 *
	 * @param dashboardAcl where widget is
	 * @param widgetAcl    widget's acl
	 * @param modifier     id of modifier
	 * @param projectRole  modifier's project role
	 * @param userRole     modifier's role
	 * @return true when widget is allowed to be deleted from db
	 */
	public static boolean isAllowedToDeleteWidget(Acl dashboardAcl, Acl widgetAcl, String modifier, ProjectRole projectRole,
			UserRole userRole) {
		return widgetAcl.getOwnerUserId().equals(modifier) || !dashboardAcl.getOwnerUserId().equals(modifier) && (
				UserRole.ADMINISTRATOR.equals(userRole) || ProjectRole.PROJECT_MANAGER.equals(projectRole));
	}

	/**
	 * Validate is specified acl owned by specified user or is shared to
	 * specified project
	 *
	 * @param acl          ACL entry
	 * @param userModifier Username
	 * @param projectName  Project Name
	 */
	@SuppressWarnings("unchecked")
	public static void isPossibleToRead(Acl acl, String userModifier, String projectName) {
		BusinessRule.expect(acl, or(Preconditions.isOwner(userModifier), Preconditions.isSharedTo(projectName)))
				.verify(
						ErrorType.UNABLE_MODIFY_SHARABLE_RESOURCE,
						Suppliers.formattedSupplier("User '{}' isn't owner of resource and resource isn't shared to project '{}'.",
								userModifier, projectName
						)
				);
	}

	/**
	 * Validate is specified acl owned by specified user or is shared to
	 * specified project but don't rise exception
	 *
	 * @param acl          ACL entry
	 * @param userModifier Username
	 * @param projectName  Project Name
	 */
	public static boolean isPossibleToReadResource(Acl acl, String userModifier, String projectName) {
		// Not resource owner
		if (!acl.getOwnerUserId().equalsIgnoreCase(userModifier)) {
			return !acl.getEntries().stream().noneMatch(Preconditions.hasACLPermission(projectName, AclPermissions.READ));
		}
		return true;
	}

	/**
	 * @param acl         ACL entry
	 * @param projectName Project Name
	 */
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

	private static Predicate<Acl> hasProjectRole(Map<String, ProjectRole> userProjects, ProjectRole role) {
		return acl -> acl.getEntries()
				.stream()
				.anyMatch(e -> userProjects.containsKey(e.getProjectId()) && userProjects.get(e.getProjectId()).compareTo(role) >= 0);
	}
}
