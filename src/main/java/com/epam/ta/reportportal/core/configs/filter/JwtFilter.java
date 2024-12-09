package com.epam.ta.reportportal.core.configs.filter;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

import com.epam.ta.reportportal.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

  @Autowired
  private JwtService jwtService;

  @Autowired
  JwtDecoder jwtDecoder;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    System.out.println("asd");
    String authHeader = request.getHeader(AUTHORIZATION);
    String token = null;
    String username = null;
    if (authHeader != null && authHeader.startsWith("Bearer")) {
      token = authHeader.substring(7);
      username = jwtService.extractUserName(token);
      Jwt jwt = jwtDecoder.decode(token);
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() != null) {

     // SecurityContextHolder.getContext().setAuthentication(new BearerTokenAuthentication());
    }

/*
      public BearerTokenAuthentication(OAuth2AuthenticatedPrincipal principal, OAuth2AccessToken
        credentials, Collection<? extends GrantedAuthority > authorities) {
      super(credentials, principal, credentials, authorities);
      Assert.isTrue(credentials.getTokenType() == TokenType.BEARER, "credentials must be a bearer token");
      this.attributes = Collections.unmodifiableMap(new LinkedHashMap(principal.getAttributes()));
      this.setAuthenticated(true);
    }
*/

    //request.getHeader()
    filterChain.doFilter(request, response);
  }
}
