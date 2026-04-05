package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.DuplicateTmsMilestoneRS;
import com.epam.reportportal.base.core.tms.dto.DuplicateTmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsMilestoneRQ;
import com.epam.reportportal.base.core.tms.dto.TmsMilestoneRS;
import com.epam.reportportal.base.core.tms.dto.TmsMilestoneStatus;
import com.epam.reportportal.base.core.tms.dto.TmsMilestoneType;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.base.core.tms.service.TmsDisplayIdService;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsMilestoneMapper {

  @Autowired
  protected TmsDisplayIdService tmsDisplayIdService;

  /**
   * Maps TmsMilestoneRQ to TmsMilestone entity.
   *
   * @param projectId   project id
   * @param milestoneRQ the milestone request
   * @return mapped TmsMilestone entity
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "productVersion", ignore = true)
  @Mapping(target = "testPlans", ignore = true)
  @Mapping(target = "project.id", source = "projectId")
  @Mapping(target = "status", source = "milestoneRQ.status", qualifiedByName = "mapStatusWithDefault",
      nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION)
  @Mapping(target = "displayId", expression = "java(tmsDisplayIdService.generateMilestoneDisplayId(projectId))")
  public abstract TmsMilestone toEntity(Long projectId, TmsMilestoneRQ milestoneRQ);

  /**
   * Maps TmsMilestone entity to TmsMilestoneRS response.
   *
   * @param milestone the milestone entity
   * @return mapped TmsMilestoneRS response
   */
  @Mapping(target = "testPlans", ignore = true)
  public abstract TmsMilestoneRS convert(TmsMilestone milestone);

  @Mapping(target = "testPlans", ignore = true)
  public abstract DuplicateTmsMilestoneRS convertDuplicateTmsMilestoneRS(TmsMilestone milestone);

  /**
   * Maps TmsMilestone entity to TmsMilestoneRS response with test plans.
   *
   * @param milestone the milestone entity
   * @param testPlans list of test plans
   * @return mapped TmsMilestoneRS response with test plans
   */
  public TmsMilestoneRS convert(TmsMilestone milestone, List<TmsTestPlanRS> testPlans) {
    var response = convert(milestone);
    response.setTestPlans(testPlans);
    return response;
  }

  /**
   * Patches TmsMilestone entity with non-null values from TmsMilestoneRQ. Only updates fields that
   * are not null in the request.
   *
   * @param projectId   project id
   * @param milestoneRQ the milestone request with updates
   * @param milestone   the target milestone entity to update
   */
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "productVersion", ignore = true)
  @Mapping(target = "testPlans", ignore = true)
  @Mapping(target = "project.id", source = "projectId")
  @Mapping(target = "status", source = "milestoneRQ.status", qualifiedByName = "mapStatus")
  @Mapping(target = "displayId", ignore = true)
  public abstract void patchEntity(Long projectId, TmsMilestoneRQ milestoneRQ,
      @MappingTarget TmsMilestone milestone);

  public DuplicateTmsMilestoneRS convertToDuplicateTmsMilestoneRS(TmsMilestone milestone,
      List<DuplicateTmsTestPlanRS> duplicateTestPlansRS) {
    var response = convertDuplicateTmsMilestoneRS(milestone);
    response.setTestPlans(duplicateTestPlansRS);
    return response;
  }

  /**
   * Converts Instant to LocalDateTime.
   *
   * @param instant the instant
   * @return LocalDateTime in UTC
   */
  protected LocalDateTime instantToLocalDateTime(Instant instant) {
    return instant != null ? LocalDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
  }

  /**
   * Converts LocalDateTime to Instant.
   *
   * @param localDateTime the local date time
   * @return Instant in UTC
   */
  protected Instant localDateTimeToInstant(LocalDateTime localDateTime) {
    return localDateTime != null ? localDateTime.toInstant(ZoneOffset.UTC) : null;
  }

  /**
   * Maps entity enum to DTO enum for TmsMilestoneType.
   *
   * @param type entity enum
   * @return DTO enum
   */
  protected TmsMilestoneType mapType(
      com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneType type) {
    return type != null ? TmsMilestoneType.valueOf(type.name()) : null;
  }

  /**
   * Maps DTO enum to entity enum for TmsMilestoneType.
   *
   * @param type DTO enum
   * @return entity enum
   */
  protected com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneType mapType(
      TmsMilestoneType type) {
    return type != null
        ? com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneType.valueOf(
        type.name()) : null;
  }

  /**
   * Maps entity enum to DTO enum for TmsMilestoneStatus.
   *
   * @param status entity enum
   * @return DTO enum
   */
  protected TmsMilestoneStatus mapStatus(
      com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneStatus status) {
    return status != null ? TmsMilestoneStatus.valueOf(status.name()) : null;
  }

  /**
   * Maps DTO enum to entity enum for TmsMilestoneStatus with default SCHEDULED.
   *
   * @param status DTO enum
   * @return entity enum
   */
  @Named("mapStatusWithDefault")
  protected com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneStatus mapStatusWithDefault(
      TmsMilestoneStatus status) {
    return status != null
        ? com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneStatus.valueOf(
        status.name())
        : com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneStatus.SCHEDULED;
  }

  /**
   * Maps DTO enum to entity enum for TmsMilestoneStatus.
   *
   * @param status DTO enum
   * @return entity enum
   */
  @Named("mapStatus")
  protected com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneStatus mapStatus(
      TmsMilestoneStatus status) {
    return status != null
        ? com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneStatus.valueOf(
        status.name()) : null;
  }
}
