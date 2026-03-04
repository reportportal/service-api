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

package com.epam.reportportal.auth.util;

import static com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils.normalizeId;

import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Authentication utils.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public final class AuthUtils {

  public static final Function<UserRole, List<GrantedAuthority>> AS_AUTHORITIES =
      userRole -> Collections.singletonList(new SimpleGrantedAuthority(userRole.getAuthority()));
  public static final UnaryOperator<String> CROP_DOMAIN =
      it -> normalizeId(StringUtils.substringBefore(it, "@"));
  public static final UnaryOperator<String> NORMALIZE_STRING =
      original -> normalizeId(original.trim());

  private AuthUtils() {
    //statics only
  }
}
