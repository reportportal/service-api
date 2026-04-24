package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.events.domain.tms.TestCaseCreatedEvent;
import com.epam.reportportal.base.core.events.domain.tms.TestCaseDeletedEvent;
import com.epam.reportportal.base.core.events.domain.tms.TestCaseUpdatedEvent;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioRequirement;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsTestCaseActivityResourceMapper {

  public TestCaseActivityResource buildActivityResource(TmsTestCase tc,
      TmsTestCaseVersion version) {
    if (tc == null) {
      return null;
    }
    var builder = TestCaseActivityResource.builder()
        .id(tc.getId())
        .projectId(tc.getTestFolder().getProject().getId())
        .name(tc.getName())
        .description(tc.getDescription())
        .priority(tc.getPriority())
        .externalId(tc.getExternalId())
        .testFolderId(tc.getTestFolder().getId())
        .build();

    mapAttributes(builder, tc);
    mapManualScenario(builder, version);

    return builder;
  }

  private void mapAttributes(TestCaseActivityResource builder, TmsTestCase tc) {
    if (CollectionUtils.isNotEmpty(tc.getAttributes())) {
      builder.setAttributes(
          tc.getAttributes().stream()
              .map(a -> a.getAttribute().getKey()
                  + (a.getAttribute().getValue() == null ? "" : ": " + a.getAttribute().getValue()))
              .toList()
      );
    }
  }

  private void mapManualScenario(TestCaseActivityResource builder, TmsTestCaseVersion version) {
    if (version == null || version.getManualScenario() == null) {
      return;
    }
    var scenario = version.getManualScenario();
    builder.setExecutionEstimationTime(scenario.getExecutionEstimationTime());
    if (scenario.getType() != null) {
      builder.setType(scenario.getType().name());
    }
    if (scenario.getPreconditions() != null) {
      builder.setPreconditions(scenario.getPreconditions().getValue());
    }
    if (scenario.getRequirements() != null) {
      builder.setRequirements(
          scenario
              .getRequirements()
              .stream()
              .map(TmsManualScenarioRequirement::getValue)
              .toList());
    }
    if (scenario.getTextScenario() != null) {
      builder.setInstructions(scenario.getTextScenario().getInstructions());
      builder.setExpectedResult(scenario.getTextScenario().getExpectedResult());
    }
    if (scenario.getStepsScenario() != null
        && CollectionUtils.isNotEmpty(scenario.getStepsScenario().getSteps())) {
      builder
          .setSteps(scenario.getStepsScenario().getSteps().stream().map(
                  s -> "Step " + s.getNumber()
                      + ": instructions: " + s.getInstructions()
                      + ": expectedResult: " + s.getExpectedResult())
              .toList());
    }
  }

  public TestCaseDeletedEvent buildTestCaseDeletedEvent(MembershipDetails membershipDetails,
      ReportPortalUser user, Long testCaseId) {
    return new TestCaseDeletedEvent(testCaseId, user.getUserId(), user.getUsername(),
        membershipDetails);
  }

  public TestCaseUpdatedEvent buildTestCaseUpdatedEvent(MembershipDetails membershipDetails,
      ReportPortalUser user, TestCaseActivityResource activityResourceBefore,
      TestCaseActivityResource activityResourceAfter) {
    return new TestCaseUpdatedEvent(
        activityResourceBefore,
        activityResourceAfter,
        user.getUserId(),
        user.getUsername(),
        membershipDetails.getOrgId());
  }

  public TestCaseCreatedEvent buildTestCaseCreatedEvent(MembershipDetails membershipDetails,
      ReportPortalUser user, TestCaseActivityResource testCaseActivityResource) {
    return new TestCaseCreatedEvent(
        testCaseActivityResource,
        user.getUserId(),
        user.getUsername(),
        membershipDetails.getOrgId()
    );
  }
}
