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
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStep;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
        .testFolderId(tc.getTestFolder().getId());

    mapTags(builder, tc);
    mapManualScenario(builder, version);

    return builder.build();
  }

  private void mapTags(TestCaseActivityResource.TestCaseActivityResourceBuilder builder,
      TmsTestCase tc) {
    if (CollectionUtils.isNotEmpty(tc.getAttributes())) {
      builder.tags(
          tc.getAttributes()
              .stream()
              .map(a -> a.getAttribute().getKey()
                  + (a.getAttribute().getValue() == null ? "" : ": " + a.getAttribute().getValue()))
              .toList()
      );
    }
  }

  private void mapManualScenario(TestCaseActivityResource.TestCaseActivityResourceBuilder builder,
      TmsTestCaseVersion version) {
    if (version == null || version.getManualScenario() == null) {
      return;
    }
    var scenario = version.getManualScenario();
    builder.executionEstimationTime(scenario.getExecutionEstimationTime());
    if (scenario.getType() != null) {
      builder.type(scenario.getType().name());
    }
    if (scenario.getPreconditions() != null) {
      builder.preconditions(appendAttachments(
          scenario.getPreconditions().getValue(),
          scenario.getPreconditions().getAttachments()));
    }
    if (scenario.getRequirements() != null) {
      builder.requirements(
          scenario
              .getRequirements()
              .stream()
              .map(TmsManualScenarioRequirement::getValue)
              .collect(java.util.stream.Collectors.joining("\n")));
    }
    if (scenario.getTextScenario() != null) {
      builder.instructions(scenario.getTextScenario().getInstructions());
      builder.expectedResult(appendAttachments(
          scenario.getTextScenario().getExpectedResult(),
          scenario.getTextScenario().getAttachments()));
    }
    if (scenario.getStepsScenario() != null
        && CollectionUtils.isNotEmpty(scenario.getStepsScenario().getSteps())) {
      builder.steps(
          scenario.getStepsScenario().getSteps().stream()
              .sorted(Comparator.comparing(TmsStep::getNumber))
              .map(s -> {
                String stepStr = "Step " + s.getNumber() + "\n"
                    + "Instructions: " + (s.getInstructions() != null ? s.getInstructions() : "") + "\n"
                    + "Expected Result: " + (s.getExpectedResult() != null ? s.getExpectedResult() : "");
                return appendAttachments(stepStr, s.getAttachments());
              })
              .collect(Collectors.joining("\n\n"))
      );
    }
  }
  
  private String appendAttachments(String text, Set<TmsAttachment> attachments) {
    if (CollectionUtils.isEmpty(attachments)) {
      return text;
    }
    String attachmentsStr = "Attachments: " + attachments.stream()
        .map(TmsAttachment::getFileName)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(", "));
    return StringUtils.isBlank(text) ? attachmentsStr : text + "\n" + attachmentsStr;
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
