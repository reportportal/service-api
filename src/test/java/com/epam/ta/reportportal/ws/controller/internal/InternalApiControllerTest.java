package com.epam.ta.reportportal.ws.controller.internal;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Set of unit tests for internal APIs
 *
 * @author Andrei Varabyeu
 */
public class InternalApiControllerTest extends BaseMvcTest {

	protected static final String INTERNAL_API_BASE_URL = "/api-internal/";

	@Test
	public void checkAllowedForComponentRole() throws Exception {
		this.mvcMock.perform(get(INTERNAL_API_BASE_URL + "/external-system/54958aec4e84859227150765").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void checkNotAllowedForAdminRole() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(AuthConstants.ADMINISTRATOR);
		this.mvcMock.perform(
				get(INTERNAL_API_BASE_URL + "/external-system/54958aec4e84859227150765").principal(AuthConstants.ADMINISTRATOR))
				.andExpect(status().isForbidden());
	}

	protected Authentication authentication() {
		return new UsernamePasswordAuthenticationToken("any", null,
				ImmutableList.<org.springframework.security.core.GrantedAuthority>builder().add(
						new SimpleGrantedAuthority("ROLE_COMPONENT")).build()
		);
	}
}