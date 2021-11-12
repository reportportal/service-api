package com.epam.ta.reportportal.auth.authenticator;

import com.epam.ta.reportportal.entity.user.User;
import com.google.common.collect.Sets;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.auth.UserRoleHierarchy.ROLE_REGISTERED;

@Service
public class RegisteredUserAuthenticator implements UserAuthenticator {

	/**
	 * Required for {@link org.springframework.security.acls.domain.AclAuthorizationStrategy#securityCheck(Acl, int)} with custom implementation
	 * {@link com.epam.ta.reportportal.auth.acl.ReportPortalAclAuthorizationStrategyImpl} to permit shared objects to the newly created user
	 *
	 * @param user {@link User}
	 * @return {@link Authentication} with authenticated user with the role {@link com.epam.ta.reportportal.auth.UserRoleHierarchy#ROLE_REGISTERED}
	 */
	@Override
	public Authentication authenticate(User user) {
		final Authentication authentication = new UsernamePasswordAuthenticationToken(user.getLogin(),
				user.getPassword(),
				Sets.newHashSet(new SimpleGrantedAuthority(ROLE_REGISTERED))
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		return authentication;
	}
}
