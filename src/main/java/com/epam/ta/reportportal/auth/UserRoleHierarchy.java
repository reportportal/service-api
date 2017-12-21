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

package com.epam.ta.reportportal.auth;

import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

/**
 * UserRoleHierarchy processor. Actually, hierarchy is pretty simple: role in
 * {@link com.epam.ta.reportportal.database.entity.user.UserRole} has more
 * rights than the following one. So, Administrator is more privileged than
 * User.
 *
 * @author Andrei Varabyeu
 */
public class UserRoleHierarchy implements RoleHierarchy {

	/**
	 * Special additional role for other microservices
	 */
	public static final String ROLE_COMPONENT = "ROLE_COMPONENT";

	private static final Logger logger = LoggerFactory.getLogger(UserRoleHierarchy.class);

	private Map<GrantedAuthority, Set<GrantedAuthority>> authoritiesMap;

	public UserRoleHierarchy() {
		authoritiesMap = Arrays.stream(UserRole.values()).collect(Collectors.toMap(this::asAuthority, this::findReachableRoles));
		/*
		 * Specify authorities explicitly. It additionally has USER role to allow other services to pass login check
		 */
		GrantedAuthority component = new SimpleGrantedAuthority(ROLE_COMPONENT);
		authoritiesMap.put(component, ImmutableSet.<GrantedAuthority>builder().add(component).build());
	}

	@Override
	public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(Collection<? extends GrantedAuthority> authorities) {

		if ((authorities == null) || (authorities.isEmpty())) {
			return AuthorityUtils.NO_AUTHORITIES;
		}

		List<GrantedAuthority> reachableRoles = authorities.stream()
				.filter(authority -> authoritiesMap.containsKey(authority))
				.flatMap(authority -> authoritiesMap.get(authority).stream())
				.collect(Collectors.toList());

		if (logger.isDebugEnabled()) {
			logger.debug("getReachableGrantedAuthorities() - From the roles " + authorities + " one can reach " + reachableRoles
					+ " in zero or more steps.");
		}

		return reachableRoles;
	}

	private Set<GrantedAuthority> findReachableRoles(UserRole authority) {
		Set<GrantedAuthority> reachableRoles = new HashSet<>();
		UserRole[] roles = UserRole.values();
		int startIndex = Arrays.binarySearch(UserRole.values(), authority);
		for (int i = 0; i <= startIndex; i++) {
			reachableRoles.add(asAuthority(roles[i]));
		}
		return reachableRoles;
	}

	private GrantedAuthority asAuthority(UserRole userRole) {
		return new SimpleGrantedAuthority(userRole.getAuthority());
	}

}