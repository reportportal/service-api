package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioAttachmentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioPreconditionsRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioRS;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioType;
import com.epam.reportportal.base.core.tms.dto.TmsRequirementRQ;
import com.epam.reportportal.base.core.tms.dto.TmsStepsManualScenarioRS;
import com.epam.reportportal.base.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTextManualScenarioRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsManualScenarioMapper implements DtoMapper<TmsManualScenario, TmsManualScenarioRS> {

  @Autowired
  private TmsTextManualScenarioMapper tmsTextManualScenarioMapper;

  @Autowired
  private TmsStepsManualScenarioMapper tmsStepsManualScenarioMapper;

  @Override
  public TmsManualScenarioRS convert(TmsManualScenario tmsManualScenario) {
    if (tmsManualScenario == null) {
      return null;
    }

    return switch (tmsManualScenario.getType()) {
      case TEXT -> tmsTextManualScenarioMapper.convert(tmsManualScenario);
      case STEPS -> tmsStepsManualScenarioMapper.convert(tmsManualScenario);
    };
  }

  @Mapping(target = "executionEstimationTime", source = "executionEstimationTime")
  @Mapping(target = "requirements", ignore = true)
  @Mapping(target = "type", source = "manualScenarioType")
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "preconditions", ignore = true)
  public abstract TmsManualScenario createTmsManualScenario(TmsManualScenarioRQ manualScenarioRQ);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "testCaseVersion", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "preconditions", ignore = true)
  @Mapping(target = "textScenario", ignore = true)
  @Mapping(target = "stepsScenario", ignore = true)
  @Mapping(target = "requirements", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy =
      NullValuePropertyMappingStrategy.SET_TO_NULL,
      nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION
  )
  public abstract void update(@MappingTarget TmsManualScenario target, TmsManualScenario source);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "testCaseVersion", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "preconditions", ignore = true)
  @Mapping(target = "textScenario", ignore = true)
  @Mapping(target = "stepsScenario", ignore = true)
  @Mapping(target = "requirements", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  public abstract void patch(@MappingTarget TmsManualScenario target, TmsManualScenario source);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "textScenario", ignore = true)
  @Mapping(target = "stepsScenario", ignore = true)
  @Mapping(target = "preconditions", ignore = true)
  @Mapping(target = "executionEstimationTime", source = "originalScenario.executionEstimationTime")
  @Mapping(target = "requirements", ignore = true)
  @Mapping(target = "type", source = "originalScenario.type")
  @Mapping(target = "testCaseVersion", source = "newVersion")
  public abstract TmsManualScenario duplicateManualScenario(TmsManualScenario originalScenario,
      TmsTestCaseVersion newVersion);

  /**
   * Checks if scenario is steps-based (TmsStepsManualScenarioRS).
   *
   * @param scenario manual scenario
   * @return true if steps-based, false otherwise
   */
  public boolean isStepsBasedScenario(TmsManualScenarioRS scenario) {
    return scenario instanceof TmsStepsManualScenarioRS;
  }

  /**
   * Checks if scenario is text-based (TmsTextManualScenarioRS).
   *
   * @param scenario manual scenario
   * @return true if text-based, false otherwise
   */
  public boolean isTextBasedScenario(TmsManualScenarioRS scenario) {
    return scenario instanceof TmsTextManualScenarioRS;
  }

  /**
   * Safely casts scenario to TmsStepsManualScenarioRS.
   *
   * @param scenario manual scenario
   * @return casted scenario or null if not instance of TmsStepsManualScenarioRS
   */
  public TmsStepsManualScenarioRS asStepsScenario(TmsManualScenarioRS scenario) {
    if (scenario instanceof TmsStepsManualScenarioRS) {
      return (TmsStepsManualScenarioRS) scenario;
    }
    return null;
  }

  /**
   * Safely casts scenario to TmsTextManualScenarioRS.
   *
   * @param scenario manual scenario
   * @return casted scenario or null if not instance of TmsTextManualScenarioRS
   */
  public TmsTextManualScenarioRS asTextScenario(TmsManualScenarioRS scenario) {
    if (scenario instanceof TmsTextManualScenarioRS) {
      return (TmsTextManualScenarioRS) scenario;
    }
    return null;
  }

  /**
   * Validates that scenario has required data.
   *
   * @param scenario manual scenario
   * @return true if scenario has data, false otherwise
   */
  public boolean isValidScenario(TmsManualScenarioRS scenario) {
    if (scenario instanceof TmsStepsManualScenarioRS stepsScenario) {
      return stepsScenario.getSteps() != null && !stepsScenario.getSteps().isEmpty();
    } else if (scenario instanceof TmsTextManualScenarioRS textScenario) {
      return textScenario.getInstructions() != null && !textScenario.getInstructions().isEmpty();
    }
    return false;
  }

  public TmsTextManualScenarioRQ convertFromRemote(RemoteTestCase remoteTestCase, List<Long> attachmentIds) {
    String rawSteps = remoteTestCase.getSteps();
    String preconditionsText = null;
    String instructionsText = rawSteps;

    if (StringUtils.isNotBlank(rawSteps)) {
      if (rawSteps.contains("*Preconditions*:")) {
        int preconditionsIdx = rawSteps.indexOf("*Preconditions*:");
        int stepsIdx = rawSteps.indexOf("*Steps*:");
        if (stepsIdx > preconditionsIdx) {
          preconditionsText = rawSteps.substring(preconditionsIdx + "*Preconditions*:".length(), stepsIdx).trim();
          instructionsText = rawSteps.substring(stepsIdx + "*Steps*:".length()).trim();
        } else {
          preconditionsText = rawSteps.substring(preconditionsIdx + "*Preconditions*:".length()).trim();
          instructionsText = "";
        }
      } else if (rawSteps.contains("*Steps*:")) {
        int stepsIdx = rawSteps.indexOf("*Steps*:");
        instructionsText = rawSteps.substring(stepsIdx + "*Steps*:".length()).trim();
      }
    }

    TmsManualScenarioPreconditionsRQ preconditionsRQ = null;
    if (StringUtils.isNotBlank(preconditionsText)) {
      preconditionsRQ = TmsManualScenarioPreconditionsRQ.builder()
          .value(preconditionsText)
          .build();
    }

    List<TmsManualScenarioAttachmentRQ> attachmentRQS;

    if (CollectionUtils.isNotEmpty(attachmentIds)) {
      attachmentRQS = attachmentIds.stream()
          .map(id -> TmsManualScenarioAttachmentRQ.builder().id(String.valueOf(id)).build())
          .toList();
    } else {
      attachmentRQS = List.of();
    }

    List<TmsRequirementRQ> requirementRQS = parseRequirements(remoteTestCase.getRequirements());

    return TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions(instructionsText)
        .expectedResult(remoteTestCase.getExpectedResults())
        .preconditions(preconditionsRQ)
        .attachments(attachmentRQS)
        .requirements(requirementRQS)
        .build();
  }

  private List<TmsRequirementRQ> parseRequirements(List<String> rawRequirements) {
    if (CollectionUtils.isEmpty(rawRequirements)) {
      return List.of();
    }

    List<TmsRequirementRQ> requirements = new ArrayList<>();
    for (String rawReq : rawRequirements) {
      if (StringUtils.isBlank(rawReq)) {
        continue;
      }
      requirements.add(TmsRequirementRQ.builder()
          .id(UUID.randomUUID().toString())
          .value(rawReq.trim())
          .build());
    }
    return requirements;
  }
}
