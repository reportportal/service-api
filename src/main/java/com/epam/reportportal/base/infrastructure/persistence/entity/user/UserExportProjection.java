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

package com.epam.reportportal.base.infrastructure.persistence.entity.user;

/**
 * Flat projection of a user row for the users export report.
 *
 * @param id                 User ID
 * @param fullName           User full name
 * @param type               User type name
 * @param email              User email
 * @param lastLogin          Raw 'last_login' value from user metadata (epoch millis), may be null
 * @param organizationsCount Number of organizations the user is a member of
 */
public record UserExportProjection(
    Long id,
    String fullName,
    String type,
    String email,
    String lastLogin,
    long organizationsCount
) {

}
