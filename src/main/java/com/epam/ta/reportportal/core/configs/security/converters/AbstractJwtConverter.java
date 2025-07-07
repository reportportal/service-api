package com.epam.ta.reportportal.core.configs.security.converters;

import com.epam.ta.reportportal.core.configs.security.JwtIssuerConfig;
import java.util.Collection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public abstract class AbstractJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  protected final UserDetailsService userDetailsService;

  protected JwtIssuerConfig config;

  protected Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter;

  protected AbstractJwtConverter(UserDetailsService userDetailsService) {
    this(userDetailsService, new JwtIssuerConfig());
  }

  protected AbstractJwtConverter(
      UserDetailsService userDetailsService,
      JwtIssuerConfig config
  ) {
    this.userDetailsService = userDetailsService;
    this.config = config;
    var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(config.getAuthoritiesClaim());
    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
    this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
  }

  protected UserDetails findUser(String identifier) {
    try {
      return userDetailsService.loadUserByUsername(identifier);
    } catch (UsernameNotFoundException e) {
      throw new UsernameNotFoundException("User not found: " + identifier, e);
    }
  }

  protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    return this.jwtGrantedAuthoritiesConverter.convert(jwt);
  }
}