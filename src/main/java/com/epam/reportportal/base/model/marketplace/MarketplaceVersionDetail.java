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
import java.util.List;

/**
 * GET /api/v1/plugins/{pluginId}/versions/{version} — manifest fields plus tier, block state,
 * advisory, sha256 and asset URLs. Unlike plugin detail it has no latestVersion.
 */
public record MarketplaceVersionDetail(
    String id,
    String name,
    String version,
    String description,
    MarketplaceAuthor author,
    String license,
    String category,
    MarketplaceCompatibility compatibility,
    String homepage,
    String access,
    String contactUrl,
    String tier,
    String pf4jId,
    boolean blocked,
    Instant blockedAt,
    String blockReason,
    MarketplaceAdvisory advisory,
    String sha256,
    String changelogUrl,
    List<String> screenshotUrls
) {

}
