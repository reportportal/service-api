/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.infrastructure.persistence.entity.user;

import java.util.UUID;

/**
 * Projection for User entity containing only authentication-related fields. This reduces query size and improves
 * performance for authentication operations.
 *
 * @param id         User ID
 * @param uuid       User UUID
 * @param externalId External system ID
 * @param login      User login
 * @param password   User password hash
 * @param email      User email
 * @param role       User role
 * @param active     Account active status
 * @param isExpired  Account expiry status
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 */
public record UserAuthProjection(
    Long id,
    UUID uuid,
    String externalId,
    String login,
    String password,
    String email,
    String role,
    Boolean active,
    boolean isExpired
) {

}
