package com.epam.reportportal.base.infrastructure.persistence.dao.project;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_USER_ID;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectProfile;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.google.common.collect.Lists;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class OrganizationProjectRepositoryTest extends BaseMvcTest {

  @Autowired
  private OrganizationProjectRepository organizationProjectRepository;


  @ParameterizedTest
  @CsvSource(value = {
      "1|name|eq|superadmin_personal",
      "1|slug|eq|superadmin-personal",
      "1|key|eq|superadmin_personal",
      "1|created_at|ne|2024-08-14T06:01:25.329026Z",
      "1|updated_at|ne|2024-08-14T06:01:25.329026Z",
      "1|users|eq|1",
      "1|launches|eq|0"
  }, delimiter = '|')
  void findAllOrganizationProjects(Long orgId, String field, String op, String value) {
    Filter filter = new Filter(ProjectProfile.class, Lists.newArrayList())
        .withCondition(
            new FilterCondition(Condition.EQUALS, false, orgId.toString(), "organization_id"))
        .withCondition(new FilterCondition(Condition.findByMarker(op).get(), false, value, field));
    Pageable pageable = PageRequest.of(0, 1, Sort.by("name"));

    Page<ProjectProfile> projectsListPage = organizationProjectRepository.getProjectProfileListByFilter(filter,
        pageable);

    assertTrue(isNotEmpty(projectsListPage.toList()));
  }

  @ParameterizedTest
  @CsvSource(value = {
      "1|1|Member",
      "2|1|Member",
  }, delimiter = '|')
  void findOrganizationProjectsAssignedToUser(Long userId, Long orgId, String role) {
    Filter filter = new Filter(ProjectProfile.class, Lists.newArrayList())
        .withCondition(new FilterCondition(Condition.EQUALS, false, orgId.toString(), "organization_id"))
        .withCondition(new FilterCondition(Condition.IN, false, "1, 2", CRITERIA_PROJECT_ID))
        .withCondition(
            new FilterCondition(Condition.EQUALS, false, userId.toString(), CRITERIA_USER_ID));
    Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));

    Page<ProjectProfile> projectsListPage = organizationProjectRepository.getProjectProfileListByFilter(filter,
        pageable);
    assertTrue(isNotEmpty(projectsListPage.toList()));
  }
}
