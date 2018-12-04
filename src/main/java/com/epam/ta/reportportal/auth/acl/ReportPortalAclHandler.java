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

import com.epam.ta.reportportal.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class ReportPortalAclHandler {

	@Autowired
	private AclService aclService;

	@Autowired
	private UserRepository userRepository;

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
	public void initAclForObject(Object object, String owner, Long projectId, boolean isShared) {
		aclService.createAcl(object);
		aclService.addPermissions(object, owner, BasePermission.ADMINISTRATION);
		if (isShared) {
			userRepository.findNamesByProject(projectId)
					.stream()
					.filter(it -> it.equalsIgnoreCase(owner))
					.forEach(login -> aclService.addPermissions(object, login, BasePermission.READ));
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
	public void updateAclObject(Object object, Long projectId, boolean isShared) {
		if (isShared) {
			userRepository.findNamesByProject(projectId).forEach(login -> aclService.addPermissions(object, login, BasePermission.READ));
		} else {
			userRepository.findNamesByProject(projectId).forEach(login -> aclService.removePermissions(object, login));
		}
	}

	/**
	 * Share concrete object for concrete user
	 *
	 * @param object   Object to share
	 * @param username User to share
	 */
	public void shareObject(Object object, String username) {
		aclService.addPermissions(object, username, BasePermission.READ);
	}

	/**
	 * Unshare concrete object for concrete user
	 *
	 * @param object   Object to share
	 * @param username User to share
	 */
	public void unShareObject(Object object, String username) {
		aclService.removePermissions(object, username);
	}

	public void deleteAclForObject(Object object) {
		aclService.deleteAcl(object);
	}

}
