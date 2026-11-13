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

package com.epam.reportportal.base.infrastructure.persistence.entity.organization;

/**
 * Filter DTO for the organization export flow. Resolves to a dedicated {@link
 * com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterTarget} that, unlike the
 * plain organization list/search target, also selects the users/projects/launches quantity and last
 * launch run aggregates required by the CSV report. Kept separate from {@link OrganizationFilter} so the
 * regular list/search endpoint does not pay for those extra joins.
 *
 * @author Siarhei Hrabko
 */
public class OrganizationExportFilter extends OrganizationFilter {

}
