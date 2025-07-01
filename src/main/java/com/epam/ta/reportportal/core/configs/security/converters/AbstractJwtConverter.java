package com.epam.ta.reportportal.core.configs.security.converters;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public abstract class AbstractJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  protected final UserDetailsService userDetailsService;

  protected Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter;

  @Value("${user-management.default-role}")
  private String defaultRole;

  protected AbstractJwtConverter(UserDetailsService userDetailsService) {
    this(userDetailsService, "authorities");
  }

  protected AbstractJwtConverter(UserDetailsService userDetailsService, String authoritiesClaimName) {
    this.userDetailsService = userDetailsService;
    var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(authoritiesClaimName);
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

  protected Collection<GrantedAuthority> getDefaultAuthorities() {
    return List.of(new SimpleGrantedAuthority(defaultRole));
  }

  protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    return this.jwtGrantedAuthoritiesConverter.convert(jwt);
  }
}