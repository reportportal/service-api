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

package com.epam.reportportal.auth.integration.converter;

import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistration;
import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistrationRestriction;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converter between database and resource representation of OAuthRegistration restrictions.
 *
 * @author Anton Machulski
 */
public class OAuthRestrictionConverter {

  private static final String ORGANIZATIONS_KEY = "organizations";
  private static final String ORGANIZATION_TYPE = "organization";

  public static final Function<OAuthRegistration, Map<String, String>> TO_RESOURCE = db -> {
    Map<String, String> restrictions = new HashMap<>();
    restrictions.put(ORGANIZATIONS_KEY, organizationsToResource(db));
    return restrictions;
  };

  private static String organizationsToResource(OAuthRegistration db) {
    if (db.getRestrictions() == null) {
      return "";
    }
    return db.getRestrictions()
        .stream()
        .filter(restriction -> ORGANIZATION_TYPE.equalsIgnoreCase(restriction.getType()))
        .map(OAuthRegistrationRestriction::getValue)
        .collect(Collectors.joining(","));
  }
}
