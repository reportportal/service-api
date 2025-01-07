package com.epam.ta.reportportal.core.configs.filter;

import com.epam.ta.reportportal.auth.ApiKeyUtils;
import com.epam.ta.reportportal.auth.ReportPortalClient;
import com.epam.ta.reportportal.auth.util.AuthUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.ApiKeyRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.user.ApiKey;
import com.google.common.collect.Maps;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

public class ApiKeyFilter {

  @Autowired
  private ApiKeyRepository apiKeyRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserDetailsService userDetailsService;


  //@Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String apiToken = request.getHeader("apiKey");
    if (StringUtils.isNotEmpty(apiToken)) {
      String hashedKey = DatatypeConverter.printHexBinary(DigestUtils.sha3_256(apiToken));
      ApiKey apiKey = apiKeyRepository.findByHash(hashedKey);

      if (ApiKeyUtils.validateToken(apiToken) && apiKeyRepository.findByHash(hashedKey) != null) {
        userRepository.findReportPortalUser(apiKey.getUserId())
            .filter(ReportPortalUser::isEnabled)
            .map(user -> {
              LocalDate today = LocalDate.now();
              if (apiKey.getLastUsedAt() == null || !apiKey.getLastUsedAt().equals(today)) {
                apiKeyRepository.updateLastUsedAt(apiKey.getId(), hashedKey, today);
              }
              return user;
            })
            .map(this::getUserWithAuthorities)
            .ifPresent(a -> authenticate(request, a));

      }
    }

    filterChain.doFilter(request, response);
  }

  private void authenticate(HttpServletRequest request, ReportPortalUser user ) {
/*    HashMap<String, String> requestParameters = new HashMap<>();
    request.put("username", user.getUsername());
    requestParameters.put("client_id", ReportPortalClient.api.name());

    Set<String> scopes = Collections.singleton(ReportPortalClient.api.name());*/

    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetailsService,
        null,
        user.getAuthorities()
    );
        authToken.setDetails(
        new WebAuthenticationDetailsSource().buildDetails(request)
    );
    SecurityContextHolder.getContext().setAuthentication(authToken);

  }

  private ReportPortalUser getUserWithAuthorities(ReportPortalUser user) {
    return ReportPortalUser.userBuilder().withUserName(user.getUsername())
        .withPassword(user.getPassword())
        .withAuthorities(AuthUtils.AS_AUTHORITIES.apply(user.getUserRole()))
        .withUserId(user.getUserId()).withUserRole(user.getUserRole())
        .withProjectDetails(Maps.newHashMapWithExpectedSize(1)).withEmail(user.getEmail()).build();
  }
}
