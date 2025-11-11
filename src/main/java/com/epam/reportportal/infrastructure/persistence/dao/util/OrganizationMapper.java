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

package com.epam.reportportal.infrastructure.persistence.dao.util;

import static com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationFilter.PROJECTS_QUANTITY;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.ORGANIZATION;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.ORGANIZATION_USER;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JUsers.USERS;

import com.epam.reportportal.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationFilter;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationProfile;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationUserAccount;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.Result;
import org.json.JSONObject;

/**
 * Set of record mappers that helps to convert the result of jooq queries into Java objects
 *
 * @author Pavel Bortnik
 */
public class OrganizationMapper {

  private static final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private OrganizationMapper() {
  }

  /**
   * Maps record into {@link Organization} object
   */
  public static final RecordMapper<? super Record, OrganizationProfile> ORGANIZATION_MAPPER = row -> {
    OrganizationProfile organization = row.into(OrganizationProfile.class);

    organization.setId(row.get(ORGANIZATION.ID, Long.class));
    organization.setCreatedAt(row.get(ORGANIZATION.CREATED_AT, Instant.class));
    organization.setUpdatedAt(row.get(ORGANIZATION.UPDATED_AT, Instant.class));
    organization.setName(row.get(ORGANIZATION.NAME, String.class));
    organization.setSlug(row.get(ORGANIZATION.SLUG, String.class));
    organization.setExternalId(row.get(ORGANIZATION.EXTERNAL_ID, String.class));
    organization.setType(row.get(ORGANIZATION.ORGANIZATION_TYPE, String.class));
    organization.setOwnerId(row.get(ORGANIZATION.OWNER_ID, Long.class));

    organization.setLaunchesQuantity(row.get(OrganizationFilter.LAUNCHES_QUANTITY, Integer.class));
    organization.setLastRun(row.get(OrganizationFilter.LAST_RUN, Instant.class));
    organization.setProjectsQuantity(row.get(PROJECTS_QUANTITY, Integer.class));
    organization.setUsersQuantity(row.get(OrganizationFilter.USERS_QUANTITY, Integer.class));

    return organization;
  };


  public static final Function<Result<? extends Record>, List<OrganizationUserAccount>> ORGANIZATION_USERS_LIST_FETCHER = rows -> {
    List<OrganizationUserAccount> userProfiles = new ArrayList<>(rows.size());

    rows.forEach(row -> {
      OrganizationUserAccount organizationUserProfile = new OrganizationUserAccount();

      organizationUserProfile.setId(row.get(ORGANIZATION_USER.USER_ID));
      organizationUserProfile.setFullName(row.get(USERS.FULL_NAME));
      organizationUserProfile.setCreatedAt(row.get(USERS.CREATED_AT, Instant.class));
      organizationUserProfile.setUpdatedAt(row.get(USERS.UPDATED_AT, Instant.class));
      organizationUserProfile.setInstanceRole(UserRole.valueOf(row.get(USERS.ROLE)));
      organizationUserProfile.setOrgRole(
          OrganizationRole.valueOf(
              row.get(ORGANIZATION_USER.ORGANIZATION_ROLE.getName(), String.class)));
      organizationUserProfile.setAuthProvider(UserType.valueOf(row.get(USERS.TYPE)));
      organizationUserProfile.setEmail(row.get(USERS.EMAIL));

      Optional.ofNullable(row.get(USERS.METADATA))
          .ifPresent(meta -> {
            // TODO: refactor after switching to jooq 3.19 with jsonb processing support
            JSONObject json = new JSONObject(row.get(USERS.METADATA).data());
            Long millis = json.optJSONObject("metadata", new JSONObject()).optLong("last_login");
            organizationUserProfile.setLastLoginAt(Instant.ofEpochMilli(millis));
          });

      Optional.ofNullable(row.get(USERS.EXTERNAL_ID))
          .ifPresent(
              extId -> organizationUserProfile.setExternalId(row.get(USERS.EXTERNAL_ID)));

      Optional.ofNullable(row.get(USERS.UUID))
          .ifPresent(
              extId -> organizationUserProfile.setUuid(row.get(USERS.UUID, UUID.class)));

      organizationUserProfile.setProjectCount(row.get(PROJECTS_QUANTITY, Integer.class));

      userProfiles.add(organizationUserProfile);

    });

    return userProfiles;
  };


}
