package com.epam.reportportal.infrastructure.persistence.dao;

import com.epam.reportportal.infrastructure.persistence.entity.organization.MembershipDetails;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectUserRepositoryCustom {

  Optional<MembershipDetails> findDetailsByUserIdAndProjectKey(Long userId, String projectKey);

  Optional<MembershipDetails> findAdminDetailsProjectKey(String projectKey);

  Page<MembershipDetails> findUserProjectsInOrganization(Long userId, Long organizationId, Pageable pageable);

  Set<Long> findUserProjectIdsInOrganization(Long userId, Long organizationId);
}
