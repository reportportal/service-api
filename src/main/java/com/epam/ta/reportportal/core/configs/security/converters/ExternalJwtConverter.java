package com.epam.ta.reportportal.core.configs.security.converters;

import com.epam.ta.reportportal.core.configs.security.MultiIdentityProviderConfig.JwtIssuerConfig;
import java.util.Collection;
import java.util.Optional;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;

public class ExternalJwtConverter extends AbstractJwtConverter {

  public ExternalJwtConverter(UserDetailsService userDetailsService, JwtIssuerConfig config) {
    super(userDetailsService, config);
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    var externalId = jwt.getClaimAsString(config.getUsernameClaim());
    var user = findUser(externalId);
    var authorities = Optional.ofNullable(extractAuthorities(jwt))
        .filter(auths -> !auths.isEmpty())
        .orElseGet(() -> (Collection<GrantedAuthority>) user.getAuthorities());

    return new UsernamePasswordAuthenticationToken(user, null, authorities);
  }
}
