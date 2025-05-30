/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.util;

import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_UUID;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_ACCOUNT_TYPE;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_EMAIL;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_EXTERNAL_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_FULL_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_INSTANCE_ROLE;

import com.epam.reportportal.api.model.AccountType;
import com.epam.reportportal.api.model.InstanceRole;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.user.User;
import com.google.common.collect.Lists;
import java.util.UUID;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public class DefaultUserFilter {

  private final Filter filter = new Filter(User.class, Lists.newArrayList());

  public DefaultUserFilter(String email, UUID uuid, String externalId, String fullName,
      InstanceRole instanceRole, AccountType accountType) {
    if (StringUtils.isNotEmpty(email)) {
      filter.withCondition(
          new FilterCondition(Condition.EQUALS, false, email, CRITERIA_EMAIL));
    }
    if (uuid != null) {
      filter.withCondition(
          new FilterCondition(Condition.EQUALS, false, uuid.toString(), CRITERIA_UUID));
    }

    if (StringUtils.isNotEmpty(externalId)) {
      filter.withCondition(
          new FilterCondition(Condition.EQUALS, false, externalId, CRITERIA_EXTERNAL_ID));
    }

    if (StringUtils.isNotEmpty(fullName)) {
      filter.withCondition(
          new FilterCondition(Condition.CONTAINS, false, fullName, CRITERIA_FULL_NAME));
    }

    if (instanceRole != null) {
      filter.withCondition(
          new FilterCondition(Condition.EQUALS, false, instanceRole.getValue(),
              CRITERIA_INSTANCE_ROLE));
    }

    if (accountType != null) {
      filter.withCondition(
          new FilterCondition(Condition.EQUALS, false, accountType.getValue(),
              CRITERIA_ACCOUNT_TYPE));
    }
  }

}

