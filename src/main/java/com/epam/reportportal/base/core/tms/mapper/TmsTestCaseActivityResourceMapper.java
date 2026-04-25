package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.events.domain.tms.TestCaseCreatedEvent;
import com.epam.reportportal.base.core.events.domain.tms.TestCaseDeletedEvent;
import com.epam.reportportal.base.core.events.domain.tms.TestCaseFieldChangedEvent;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.core.tms.mapper.processor.TmsTestCaseFieldProcessor;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioRequirement;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStep;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsTestCaseActivityResourceMapper {

  @Autowired
  private List<TmsTestCaseFieldProcessor> tmsTestCaseFieldProcessors;

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
      builder.preconditions(scenario.getPreconditions().getValue());
      builder.preconditionsAttachments(
          getAttachmentsString(scenario.getPreconditions().getAttachments()));
    }
    if (scenario.getRequirements() != null) {
      builder.requirements(
          scenario
              .getRequirements()
              .stream()
              .map(TmsManualScenarioRequirement::getValue)
              .collect(Collectors.joining("\n")));
    }
    if (scenario.getTextScenario() != null) {
      builder.instructions(scenario.getTextScenario().getInstructions());
      builder.expectedResult(scenario.getTextScenario().getExpectedResult());
      builder.manualScenarioAttachments(
          getAttachmentsString(scenario.getTextScenario().getAttachments()));
    }
    if (scenario.getStepsScenario() != null
        && CollectionUtils.isNotEmpty(scenario.getStepsScenario().getSteps())) {
      builder.steps(
          scenario.getStepsScenario().getSteps().stream()
              .sorted(Comparator.comparing(TmsStep::getNumber))
              .map(s -> "Step " + s.getNumber() + "\n"
                  + "Instructions: " + (s.getInstructions() != null ? s.getInstructions() : "")
                  + "\n"
                  + "Expected Result: " + (s.getExpectedResult() != null ? s.getExpectedResult()
                  : ""))
              .collect(Collectors.joining("\n\n"))
      );
      builder.manualScenarioStepAttachments(
          scenario.getStepsScenario().getSteps().stream()
              .sorted(Comparator.comparing(TmsStep::getNumber))
              .map(s -> "Step " + s.getNumber() + "\n" + getAttachmentsString(s.getAttachments()))
              .filter(s -> !s.endsWith("\n"))
              .collect(Collectors.joining("\n\n"))
      );
    }
  }

  private String getAttachmentsString(Set<TmsAttachment> attachments) {
    if (CollectionUtils.isEmpty(attachments)) {
      return "";
    }
    return "Attachments: " + attachments.stream()
        .map(TmsAttachment::getFileName)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(", "));
  }

  public TestCaseDeletedEvent buildTestCaseDeletedEvent(MembershipDetails membershipDetails,
      ReportPortalUser user, Long testCaseId) {
    return new TestCaseDeletedEvent(testCaseId, user.getUserId(), user.getUsername(),
        membershipDetails);
  }
  
  public List<TestCaseFieldChangedEvent> buildTestCaseFieldChangedEvents(
      MembershipDetails membershipDetails,
      ReportPortalUser user,
      TestCaseActivityResource before,
      TestCaseActivityResource after) {
  
    return tmsTestCaseFieldProcessors
        .stream()
        .map(processor -> processor.process(before, after))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .peek(event -> event.setContext(user.getUserId(), user.getUsername(), membershipDetails.getOrgId()))
        .collect(Collectors.toList());
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
