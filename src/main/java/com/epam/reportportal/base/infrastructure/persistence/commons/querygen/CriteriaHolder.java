/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.infrastructure.persistence.commons.querygen;

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.query.JoinEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LogLevel;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemIssueGroup;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JIntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JLaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JOrganizationRoleEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JStatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JTestItemTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.util.DateTimeUtils;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.BooleanUtils;
import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 * Holds mapping between request search criteria and DB engine search criteria. Should be used for conversion request
 * query parameters to DB engine query parameters.
 *
 * @author Andrei Varabyeu
 */
public class CriteriaHolder {

  /**
   * Criteria from search string
   */
  private String filterCriteria;
  /**
   * Internal Criteria to internal search be performed
   */
  private String queryCriteria;
  /**
   * Aggregate criteria for filtering. Only for fields that should be aggregated. If not - used default queryCriteria
   */
  private String aggregateCriteria;
  private Class<?> dataType;
  private List<JoinEntity> joinChain = Lists.newArrayList();

  /**
   * If true - criteria field will be not added in the list of selected fields. Used in case when field is calculated in
   * selection part to avoid field duplication.
   */
  private boolean ignoreSelect;

  public CriteriaHolder() {
    // added for deserialization from DB
  }

  public CriteriaHolder(String filterCriteria, String queryCriteria, Class<?> dataType) {
    this.filterCriteria = Preconditions.checkNotNull(filterCriteria,
        "Filter criteria should not be null");
    this.queryCriteria = Preconditions.checkNotNull(queryCriteria,
        "Filter criteria should not be null");
    this.aggregateCriteria = queryCriteria;
    this.dataType = Preconditions.checkNotNull(dataType, "Data type should not be null");
  }

  public CriteriaHolder(String filterCriteria, String queryCriteria, Class<?> dataType,
      List<JoinEntity> joinChain) {
    this.filterCriteria = Preconditions.checkNotNull(filterCriteria,
        "Filter criteria should not be null");
    this.queryCriteria = Preconditions.checkNotNull(queryCriteria,
        "Filter criteria should not be null");
    this.aggregateCriteria = queryCriteria;
    this.dataType = Preconditions.checkNotNull(dataType, "Data type should not be null");
    this.joinChain = Preconditions.checkNotNull(joinChain, "Join chain should not be null");
  }

  public CriteriaHolder(String filterCriteria, Field queryCriteria, Class<?> dataType) {
    this.filterCriteria = Preconditions.checkNotNull(filterCriteria,
        "Filter criteria should not be null");
    this.queryCriteria = Preconditions.checkNotNull(queryCriteria,
        "Filter criteria should not be null").getQualifiedName().toString();
    this.aggregateCriteria = queryCriteria.getQualifiedName().toString();
    this.dataType = Preconditions.checkNotNull(dataType, "Data type should not be null");
  }

  public CriteriaHolder(String filterCriteria, Field queryCriteria, Class<?> dataType,
      List<JoinEntity> joinChain) {
    this.filterCriteria = Preconditions.checkNotNull(filterCriteria,
        "Filter criteria should not be null");
    this.queryCriteria = Preconditions.checkNotNull(queryCriteria,
        "Filter criteria should not be null").getQualifiedName().toString();
    this.aggregateCriteria = queryCriteria.getQualifiedName().toString();
    this.dataType = Preconditions.checkNotNull(dataType, "Data type should not be null");
    this.joinChain = Preconditions.checkNotNull(joinChain, "Join chain should not be null");
  }

  public String getFilterCriteria() {
    return filterCriteria;
  }

  public String getQueryCriteria() {
    return queryCriteria;
  }

  public String getAggregateCriteria() {
    return aggregateCriteria;
  }

  public void setAggregateCriteria(String aggregateCriteria) {
    this.aggregateCriteria = aggregateCriteria;
  }

  public Class<?> getDataType() {
    return dataType;
  }

  public List<JoinEntity> getJoinChain() {
    return joinChain;
  }

  public Object castValue(String oneValue) {
    return this.castValue(oneValue, ErrorType.INCORRECT_FILTER_PARAMETERS);
  }

  /**
   * Casting provided criteriaHolder by specified {@link Class} for specified value.
   * <p>
   * NOTE:<br> errorType - error which should be thrown when unable cast value
   *
   * @param oneValue  Value to cast
   * @param errorType ErrorType in case of error
   * @return Casted value
   */
  public Object castValue(String oneValue, ErrorType errorType) {
    Object castedValue;
    if (Number.class.isAssignableFrom(getDataType())) {
      /* Verify correct number */
      castedValue = parseLong(oneValue, errorType);
    } else if (Date.class.isAssignableFrom(getDataType())) {

      if (FilterRules.dateInMillis().test(oneValue)) {
        castedValue = Instant.ofEpochMilli(Long.parseLong(oneValue));
      } else {

        try {
          castedValue = DateTimeUtils.parseDateTimeWithOffset(oneValue);
        } catch (DateTimeParseException e) {
          throw new ReportPortalException(errorType,
              formattedSupplier("Cannot convert '{}' to valid date", oneValue).get()
          );
        }
      }
    } else if (boolean.class.equals(getDataType()) || Boolean.class.isAssignableFrom(
        getDataType())) {
      castedValue = BooleanUtils.toBoolean(oneValue);
    } else if (LogLevel.class.isAssignableFrom(getDataType())) {
      castedValue = parseLong(oneValue, errorType);
    } else if (JStatusEnum.class.isAssignableFrom(getDataType())) {

      Optional<StatusEnum> status = StatusEnum.fromValue(oneValue);
      BusinessRule.expect(status, Optional::isPresent)
          .verify(errorType,
              formattedSupplier("Cannot convert '{}' to valid 'Status'", oneValue));
      castedValue = JStatusEnum.valueOf(status.get().name());

    } else if (JTestItemTypeEnum.class.isAssignableFrom(getDataType())) {

      Optional<TestItemTypeEnum> itemType = TestItemTypeEnum.fromValue(oneValue);
      BusinessRule.expect(itemType, Optional::isPresent)
          .verify(errorType,
              formattedSupplier("Cannot convert '{}' to valid 'Test item type'",
                  oneValue));
      castedValue = JTestItemTypeEnum.valueOf(itemType.get().name());

    } else if (JLaunchModeEnum.class.isAssignableFrom(getDataType())) {

      Optional<LaunchModeEnum> launchMode = LaunchModeEnum.findByName(oneValue);
      BusinessRule.expect(launchMode, Optional::isPresent)
          .verify(errorType,
              formattedSupplier("Cannot convert '{}' to valid 'Launch mode'", oneValue));
      castedValue = JLaunchModeEnum.valueOf(launchMode.get().name());

    } else if (JIntegrationGroupEnum.class.isAssignableFrom(getDataType())) {

      Optional<IntegrationGroupEnum> integrationGroup = IntegrationGroupEnum.findByName(oneValue);
      BusinessRule.expect(integrationGroup, Optional::isPresent)
          .verify(errorType,
              formattedSupplier("Cannot convert '{}' to valid 'Integration group",
                  oneValue));
      castedValue = JIntegrationGroupEnum.valueOf(integrationGroup.get().name());

    } else if (JOrganizationRoleEnum.class.isAssignableFrom(getDataType())) {

      Optional<OrganizationRole> organizationRole = OrganizationRole.forName(oneValue);
      BusinessRule.expect(organizationRole, Optional::isPresent)
          .verify(errorType,
              formattedSupplier("Cannot convert '{}' to valid 'Organization role'", oneValue));
      castedValue = JOrganizationRoleEnum.valueOf(organizationRole.get().name());

    } else if (TestItemIssueGroup.class.isAssignableFrom(getDataType())) {
      castedValue = TestItemIssueGroup.validate(oneValue);
      BusinessRule.expect(castedValue, Objects::nonNull)
          .verify(errorType,
              formattedSupplier("Cannot convert '{}' to valid 'Issue Type'", oneValue));
    } else if (Collection.class.isAssignableFrom(getDataType())) {
      /* Collection doesn't stores objects as ObjectId */
      castedValue = oneValue;
    } else if (String.class.isAssignableFrom(getDataType())) {
      castedValue = oneValue != null ? oneValue.trim() : null;
    } else if (UUID.class.isAssignableFrom(getDataType())) {
      try {
        castedValue = UUID.fromString(oneValue);
      } catch (IllegalArgumentException e) {
        throw new ReportPortalException(errorType,
            formattedSupplier("Cannot convert '{}' to valid 'UUID'", oneValue));
      }
    } else {
      castedValue = DSL.val(oneValue).cast(getDataType());
    }

    return castedValue;
  }

  private Long parseLong(String value, ErrorType errorType) {
    try {
      return Long.parseLong(value);
    } catch (final NumberFormatException nfe) {
      throw new ReportPortalException(errorType,
          formattedSupplier("Cannot convert '{}' to valid number", value));
    }
  }

}
