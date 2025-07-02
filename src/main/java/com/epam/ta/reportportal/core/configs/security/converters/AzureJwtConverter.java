package com.epam.ta.reportportal.core.configs.security.converters;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;

public class AzureJwtConverter extends AbstractJwtConverter {

  public AzureJwtConverter(UserDetailsService userDetailsService) {
    super(userDetailsService, "roles");
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    var username = jwt.getClaimAsString("upn") != null
        ? jwt.getClaimAsString("upn")
        : jwt.getClaimAsString("preferred_username");
    var user = findUser(username);
    var authorities = extractAuthorities(jwt);

    return new UsernamePasswordAuthenticationToken(user, null, authorities);
  }
}
