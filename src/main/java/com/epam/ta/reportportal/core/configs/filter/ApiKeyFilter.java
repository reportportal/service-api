package com.epam.ta.reportportal.core.configs.filter;

import com.epam.ta.reportportal.auth.ApiKeyUtils;
import com.epam.ta.reportportal.auth.util.AuthUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.ApiKeyRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.user.ApiKey;
import com.google.common.collect.Maps;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.time.LocalDate;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

  @Autowired
  private ApiKeyRepository apiKeyRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserDetailsService userDetailsService;


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String tokenValue = request.getHeader("apiKey");
    if (tokenValue != null) {
      if (ApiKeyUtils.validateToken(tokenValue)) {
        RememberMeAuthenticationToken authToken = new RememberMeAuthenticationToken(tokenValue,
            userDetailsService,
            null
        );
        authToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        String hashedKey = DatatypeConverter.printHexBinary(DigestUtils.sha3_256(tokenValue));
        ApiKey apiKey = apiKeyRepository.findByHash(hashedKey);
        if (apiKey != null) {
          LocalDate today = LocalDate.now();
          if (apiKey.getLastUsedAt() == null || !apiKey.getLastUsedAt().equals(today)) {
            apiKeyRepository.updateLastUsedAt(apiKey.getId(), hashedKey, today);
          }
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private ReportPortalUser getUserWithAuthorities(ReportPortalUser user) {
    return ReportPortalUser.userBuilder().withUserName(user.getUsername())
        .withPassword(user.getPassword())
        .withAuthorities(AuthUtils.AS_AUTHORITIES.apply(user.getUserRole()))
        .withUserId(user.getUserId()).withUserRole(user.getUserRole())
        .withProjectDetails(Maps.newHashMapWithExpectedSize(1)).withEmail(user.getEmail()).build();
  }
}
