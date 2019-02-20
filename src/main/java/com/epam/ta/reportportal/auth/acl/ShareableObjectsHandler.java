/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.auth.acl;

import com.epam.ta.reportportal.dao.ShareableEntityRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.ShareableEntity;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class ShareableObjectsHandler {

	@Autowired
	private ReportPortalAclService aclService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ShareableEntityRepository shareableEntityRepository;

	/**
	 * Initialize acl for sharable object. Give {@link BasePermission#ADMINISTRATION}
	 * permissions to owner. If object is shared, give {@link BasePermission#READ}
	 * permissions to users assigned to project.
	 *
	 * @param object    Object for acl
	 * @param owner     Owner of object
	 * @param projectId Project id
	 * @param isShared  Shared or not
	 */
	public void initAcl(Object object, String owner, Long projectId, boolean isShared) {
		aclService.createAcl(object);
		aclService.addPermissions(object, owner, BasePermission.ADMINISTRATION);
		if (isShared) {
			userRepository.findUsernamesWithProjectRolesByProjectId(projectId)
					.entrySet()
					.stream()
					.filter(entry -> !entry.getKey().equalsIgnoreCase(owner))
					.forEach(entry -> {
						if (ProjectRole.PROJECT_MANAGER.higherThan(entry.getValue())) {
							aclService.addPermissions(object, entry.getKey(), BasePermission.READ);
						} else {
							aclService.addPermissions(object, entry.getKey(), BasePermission.ADMINISTRATION);
						}

					});
		}
	}

	/**
	 * Update acl for sharable object. If object is shared, give {@link BasePermission#READ}
	 * permissions to users assigned to project.
	 *
	 * @param object    Object for acl
	 * @param projectId Project id
	 * @param isShared  Shared or not
	 */
	public void updateAcl(Object object, Long projectId, boolean isShared) {
		if (isShared) {
			userRepository.findUsernamesWithProjectRolesByProjectId(projectId).forEach((key, value) -> {
				if (ProjectRole.PROJECT_MANAGER.higherThan(value)) {
					aclService.addPermissions(object, key, BasePermission.READ);
				} else {
					aclService.addPermissions(object, key, BasePermission.ADMINISTRATION);
				}

			});
		} else {
			userRepository.findNamesByProject(projectId).forEach(login -> aclService.removePermissions(object, login));
		}
	}

	/**
	 * Prevent shared objects for concrete user
	 *
	 * @param projectId Project
	 * @param userName  Username
	 */
	public void preventSharedObjects(Long projectId, String userName) {
		List<ShareableEntity> sharedEntities = shareableEntityRepository.findAllByProjectIdAndShared(projectId, true);
		sharedEntities.forEach(entity -> aclService.removePermissions(entity, userName));
	}

	/**
	 * Permit shared objects for concrete user
	 *
	 * @param projectId Project
	 * @param userName  Username
	 */
	public void permitSharedObjects(Long projectId, String userName, Permission permission) {
		List<ShareableEntity> shareableEntities = shareableEntityRepository.findAllByProjectIdAndShared(projectId, true);
		shareableEntities.forEach(entity -> aclService.addPermissions(entity, userName, permission));
	}

	/**
	 * Remove ACL for object.
	 *
	 * @param object Object to be removed
	 */
	public void deleteAclForObject(Object object) {
		aclService.deleteAcl(object);
	}

}
