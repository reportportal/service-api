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

package com.epam.ta.reportportal.auth;

import com.epam.ta.reportportal.entity.user.UserRole;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * UserRoleHierarchy processor. Actually, hierarchy is pretty simple: role in {@link UserRole} has
 * more rights than the following one. So, Administrator is more privileged than User.
 *
 * @author Andrei Varabyeu
 */
@Slf4j
public class UserRoleHierarchy implements RoleHierarchy {

  public static final String ROLE_REGISTERED = "ROLE_REGISTERED";

  /**
   * Special additional role for other microservices
   */
  public static final String ROLE_COMPONENT = "ROLE_COMPONENT";

  private Map<GrantedAuthority, Set<GrantedAuthority>> authoritiesMap;

  public UserRoleHierarchy() {
    authoritiesMap = Arrays.stream(UserRole.values())
        .collect(Collectors.toMap(this::asAuthority, this::findReachableRoles));
    /*
     * Specify authorities explicitly. It additionally has USER role to allow other services to pass login check
     */
    GrantedAuthority component = new SimpleGrantedAuthority(ROLE_COMPONENT);
    authoritiesMap.put(component, ImmutableSet.<GrantedAuthority>builder().add(component).build());
  }

  @Override
  public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
      Collection<? extends GrantedAuthority> authorities) {

    if ((authorities == null) || (authorities.isEmpty())) {
      return AuthorityUtils.NO_AUTHORITIES;
    }

    List<GrantedAuthority> reachableRoles = authorities.stream()
        .filter(authority -> authoritiesMap.containsKey(authority))
        .flatMap(authority -> authoritiesMap.get(authority).stream())
        .collect(Collectors.toList());


      log.debug(
          "getReachableGrantedAuthorities() - From the roles {} one can reach {} in zero or more steps.",
          authorities, reachableRoles);


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
