package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_RETENTION_POLICY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.RetentionPolicyEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/db/fill/launch/launch-retention-policy-filtering.sql")
class LaunchRetentionPolicyFilteringTest extends BaseMvcTest {

  @Autowired
  private LaunchRepository launchRepository;

  @Test
  void equalsRetentionPolicy() {
    List<Launch> important = find(FilterCondition.builder()
        .eq(CRITERIA_RETENTION_POLICY, "important")
        .build());

    assertEquals(List.of(1L), ids(important));
    assertEquals(RetentionPolicyEnum.IMPORTANT, important.get(0).getRetentionPolicy());

    List<Launch> regular = find(FilterCondition.builder()
        .eq(CRITERIA_RETENTION_POLICY, "REGULAR")
        .build());

    assertEquals(List.of(2L), ids(regular));
    assertEquals(RetentionPolicyEnum.REGULAR, regular.get(0).getRetentionPolicy());
  }

  @Test
  void notEqualsRetentionPolicy() {
    List<Launch> launches = find(FilterCondition.builder()
        .withCondition(Condition.NOT_EQUALS)
        .withSearchCriteria(CRITERIA_RETENTION_POLICY)
        .withValue("IMPORTANT")
        .build());

    assertEquals(List.of(2L), ids(launches));
  }

  @Test
  void inRetentionPolicy() {
    List<Launch> launches = find(FilterCondition.builder()
        .in(CRITERIA_RETENTION_POLICY, List.of("IMPORTANT", "regular"))
        .build());

    assertEquals(Set.of(1L, 2L), Set.copyOf(ids(launches)));
  }

  @Test
  void rejectsUnknownRetentionPolicy() {
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> find(FilterCondition.builder()
            .eq(CRITERIA_RETENTION_POLICY, "UNKNOWN")
            .build()));

    assertEquals(ErrorType.INCORRECT_FILTER_PARAMETERS, exception.getErrorType());
    assertTrue(exception.getMessage().contains("UNKNOWN"));
  }

  private List<Launch> find(FilterCondition condition) {
    return launchRepository.findByFilter(Filter.builder()
        .withTarget(Launch.class)
        .withCondition(condition)
        .build());
  }

  private static List<Long> ids(List<Launch> launches) {
    return launches.stream().map(Launch::getId).collect(Collectors.toList());
  }
}
