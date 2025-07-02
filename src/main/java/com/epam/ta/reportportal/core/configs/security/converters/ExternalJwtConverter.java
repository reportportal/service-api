package com.epam.ta.reportportal.core.configs.security.converters;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;

public class ExternalJwtConverter extends AbstractJwtConverter {

  public ExternalJwtConverter(UserDetailsService userDetailsService) {
    super(userDetailsService);
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    var externalId = jwt.getClaimAsString("sub");
    var user = findUser(externalId);
    var authorities = extractAuthorities(jwt);

    return new UsernamePasswordAuthenticationToken(user, null, authorities);
  }
}
