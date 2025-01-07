package com.epam.ta.reportportal.core.configs.filter;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

  @Autowired
  private JwtService jwtService;

  @Autowired
  private UserDetailsService userDetailsService;


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String authHeader = request.getHeader(AUTHORIZATION);
    String token = null;
    String username = null;

    if (authHeader != null && authHeader.toLowerCase().startsWith("bearer")) {
      token = authHeader.substring(7);
      username = jwtService.extractUserName(token);
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      BusinessRule.expect(jwtService.isValid(token), BooleanUtils::isTrue)
          .verify(ErrorType.FORBIDDEN_OPERATION, "User token expired");

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
          userDetails,
          null,
          userDetails.getAuthorities()
      );
      authToken.setDetails(
          new WebAuthenticationDetailsSource().buildDetails(request)
      );

      SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    filterChain.doFilter(request, response);
  }
}
