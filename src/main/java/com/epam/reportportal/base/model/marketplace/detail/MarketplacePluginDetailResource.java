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

package com.epam.reportportal.base.model.marketplace.detail;

import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceAdvisoryResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceBlockedResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceRemovedResource;
import java.util.List;

/**
 * The marketplace half of a single plugin's page. Nothing local is in here: what is installed
 * comes from the catalogue, and this answer is only what the registry says.
 *
 * <p>A removed plugin is not a missing one. The registry answers its tombstone, and it arrives
 * here as {@code removed} on an otherwise empty answer, so the page can say "removed from the
 * marketplace, still running here" instead of "no such plugin".
 *
 * @param id            registry plugin id
 * @param name          display name, null for a removed plugin
 * @param description   short description, null for a removed plugin
 * @param latestVersion latest published version, null for a removed plugin
 * @param access        {@code public} or {@code premium}
 * @param tier          trust tier
 * @param versions      version history, never null, empty for a removed plugin
 * @param changelog     changelog of the latest version, or null
 * @param screenshots   screenshot URLs of the latest version, never null
 * @param advisory      advisory on the latest version, or null
 * @param blocked       block state of the latest version, or null
 * @param removed       registry tombstone, or null
 * @param locked        premium and no licence configured on this instance
 */
public record MarketplacePluginDetailResource(
    String id,
    String name,
    String description,
    String latestVersion,
    String access,
    String tier,
    List<MarketplaceVersionResource> versions,
    MarketplaceChangelogResource changelog,
    List<String> screenshots,
    MarketplaceAdvisoryResource advisory,
    MarketplaceBlockedResource blocked,
    MarketplaceRemovedResource removed,
    boolean locked
) {

}
