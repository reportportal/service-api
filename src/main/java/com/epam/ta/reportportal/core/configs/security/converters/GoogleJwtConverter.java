package com.epam.ta.reportportal.core.configs.security.converters;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;

public class GoogleJwtConverter extends AbstractJwtConverter {

  public GoogleJwtConverter(UserDetailsService userDetailsService) {
    super(userDetailsService);
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    var email = jwt.getClaimAsString("email");
    var user = findUser(email);
    var authorities = getDefaultAuthorities();

    return new UsernamePasswordAuthenticationToken(user, null, authorities);
  }
}
