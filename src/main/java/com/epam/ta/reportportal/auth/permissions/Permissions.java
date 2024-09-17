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

package com.epam.ta.reportportal.auth.permissions;

/**
 * Set of constants related to permissions
 *
 * @author Andrei Varabyeu
 */
public final class Permissions {

  private Permissions() {
    // constants holder
  }

  public static final String IS_ADMIN = "hasRole('ADMINISTRATOR')";

  public static final String ALLOWED_TO_EDIT_USER =
      "(#login.toLowerCase() == authentication.name)" + "||" + IS_ADMIN;

  public static final String ORGANIZATION_MANAGER =
      "hasPermission(#orgId, 'organizationManager')" + "||" + IS_ADMIN;

  public static final String ORGANIZATION_MEMBER =
      "hasPermission(#orgId, 'organizationMember')" + "||" + IS_ADMIN;

  public static final String ALLOWED_TO_EDIT_PROJECT =
      "hasPermission(#projectKey.toLowerCase(), 'allowedToEditProject')" + "||" + IS_ADMIN;

  public static final String ALLOWED_TO_VIEW_PROJECT =
      "hasPermission(#projectKey.toLowerCase(), 'allowedToViewProject')" + "||" + IS_ADMIN;

  public static final String INVITATION_ALLOWED = IS_ADMIN + "||"
      + "hasPermission(#invitationRequest, 'invitationAllowed')";
}
