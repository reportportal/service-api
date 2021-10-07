package com.epam.ta.reportportal.auth.authenticator;

import com.epam.ta.reportportal.entity.user.User;
import org.springframework.security.core.Authentication;

public interface UserAuthenticator {

	Authentication authenticate(User user);
}
