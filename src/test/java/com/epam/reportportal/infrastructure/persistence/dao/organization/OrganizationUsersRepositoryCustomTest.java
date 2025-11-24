package com.epam.reportportal.infrastructure.persistence.dao.organization;

import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.epam.reportportal.ws.BaseMvcTest;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationUserAccount;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationUserFilter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class OrganizationUsersRepositoryCustomTest extends BaseMvcTest {

  @Autowired
  OrganizationUsersRepositoryCustom organizationUsersRepositoryCustom;

  @Test
  void findOrganizationUsersByFilter() {
    Filter filter = new Filter(OrganizationUserFilter.class,
        Condition.EQUALS,
        false,
        "1",
        "organization_id");

    final List<OrganizationUserAccount> orgUsers = organizationUsersRepositoryCustom.findByFilter(
        filter);
    assertFalse(orgUsers.isEmpty());
  }

  @ParameterizedTest
  @CsvSource(value = {
      "0|1|fullName",
      "0|2|fullName",
  }, delimiter = '|')
  void findOrganizationUsersByFilterPageable(int offset, int limit, String sortField) {
    Filter filter = new Filter(OrganizationUserFilter.class,
        Condition.EQUALS,
        false,
        "1",
        CRITERIA_ORG_ID);
    filter.withCondition(
        new FilterCondition(Condition.IN, false, "1, 2", CRITERIA_PROJECT_ID));
    PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by(Sort.Direction.DESC, sortField));

    final Page<OrganizationUserAccount> orgUsers = organizationUsersRepositoryCustom.findByFilter(
        filter, pageRequest);
    assertEquals(limit, orgUsers.getContent().size());
  }
}
