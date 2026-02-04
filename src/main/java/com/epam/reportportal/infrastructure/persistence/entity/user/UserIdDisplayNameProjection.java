/*
 * Copyright 2026 EPAM Systems
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

/**
 * Projection for User entity containing ID and display name. Display name is the user's full name if available,
 * otherwise the login.
 *
 * @param id          User ID
 * @param displayName User display name (fullName or login if fullName is null)
 */
public record UserIdDisplayNameProjection(
    Long id,
    String displayName
) {

}
