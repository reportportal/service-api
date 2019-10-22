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

package com.epam.ta.reportportal.auth.acl;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.epam.ta.reportportal.auth.UserRoleHierarchy.ROLE_REGISTERED;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ReportPortalAclAuthorizationStrategyImpl extends AclAuthorizationStrategyImpl {

	public ReportPortalAclAuthorizationStrategyImpl(GrantedAuthority... auths) {
		super(auths);
	}

	@Override
	public void securityCheck(Acl acl, int changeType) {

		if ((SecurityContextHolder.getContext() == null) || (SecurityContextHolder.getContext().getAuthentication() == null) || !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
			throw new AccessDeniedException("Authenticated principal required to operate with ACLs");
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (!authentication.isAuthenticated() || !isJustRegistered(authentication)) {
			super.securityCheck(acl, changeType);
		}
	}

	private boolean isJustRegistered(Authentication authentication) {
		return authentication.getAuthorities().stream().anyMatch(authority -> ROLE_REGISTERED.equals(authority.getAuthority()));
	}
}
