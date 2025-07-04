package com.epam.ta.reportportal.auth.userdetails;

import static com.epam.ta.reportportal.auth.converter.ReportPortalUserConverter.TO_REPORT_PORTAL_USER;

import com.epam.ta.reportportal.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring's {@link UserDetailsService} implementation for external users. Uses
 * {@link com.epam.ta.reportportal.entity.user.User} entity from ReportPortal database.
 */
@Service
public class ExternalUserDetailsService implements UserDetailsService {

  private UserRepository userRepository;

  @Autowired
  public void setUserRepository(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String externalId) throws UsernameNotFoundException {
    return userRepository.findByExternalId(externalId)
        .map(TO_REPORT_PORTAL_USER)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }
}
