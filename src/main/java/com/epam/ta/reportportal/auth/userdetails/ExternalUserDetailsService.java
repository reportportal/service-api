package com.epam.ta.reportportal.auth.userdetails;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;

import com.epam.ta.reportportal.auth.util.AuthUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.UserRepository;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExternalUserDetailsService implements UserDetailsService {
  private UserRepository userRepository;

  @Autowired
  public void setUserRepository(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    //TODO: Change to findByExternalId when it is implemented
    var user = userRepository.findByEmail(normalizeId(username))
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    UserDetails userDetails = User.builder()
        .username(user.getEmail())
        .password(user.getPassword() == null ? "" : user.getPassword())
        .authorities(AuthUtils.AS_AUTHORITIES.apply(user.getRole()))
        .build();

    return ReportPortalUser.userBuilder()
        .withUserDetails(userDetails)
        .withUserId(user.getId())
        .withUserRole(user.getRole())
        .withOrganizationDetails(Maps.newHashMapWithExpectedSize(1))
        .withEmail(user.getEmail())
        .build();
  }

}
