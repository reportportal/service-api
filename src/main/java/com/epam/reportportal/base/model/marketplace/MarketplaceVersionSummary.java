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

package com.epam.reportportal.base.model.marketplace;

import java.time.Instant;

/**
 * One entry of GET /api/v1/plugins/{pluginId}/versions.
 *
 * <p>Carries the declared compatibility range and the advisory attached to this specific version:
 * a caller rendering a table of versions needs both per row, and the per-version detail route
 * would answer one request per row.
 *
 * <p>A null {@code compatibility} means the entry declares no range — published before the
 * registry recorded one — and that is unknown rather than compatible.
 */
public record MarketplaceVersionSummary(
    String version,
    Instant publishedAt,
    boolean blocked,
    Instant blockedAt,
    String blockReason,
    MarketplaceCompatibility compatibility,
    MarketplaceAdvisory advisory
) {

}
