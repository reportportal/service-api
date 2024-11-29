package com.epam.ta.reportportal.auth.permissions;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("authenticatedUserPermission")
@LookupPermission({"authenticated"})
public class Authenticated implements Permission {

  @Override
  public boolean isAllowed(Authentication authentication, Object invitationRequest) {
    return authentication.isAuthenticated();
  }
}
