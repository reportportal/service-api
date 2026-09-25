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
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceCatalogueResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceRemovedResource;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatusResource;
import java.util.List;

/**
 * The marketplace half of a single plugin's page. Nothing local is in here: what is installed
 * comes from the catalogue, and this answer is only what the registry says.
 *
 * <p>It carries the same {@code registry} envelope
 * {@link MarketplaceCatalogueResource} does, and for the same reason: the UI has one rule for
 * whether a marketplace-sourced signal may be believed, and that rule reads the envelope. A
 * response that could not say "the registry was unreachable" would force the page to grow a
 * second rule, and two rules drift.
 *
 * <p>Offline is therefore a 200 with everything registry-derived absent, not an error. A removed
 * plugin is not a missing one either: the registry answers its tombstone, and it arrives here as
 * {@code removed} beside a {@code plugin} that carries only its id, so the page can say "removed
 * from the marketplace, still running here" instead of "no such plugin".
 *
 * @param registry    registry reachability and host, never null
 * @param plugin      the registry's manifest for it, null when the registry could not be asked
 * @param versions    version history, never null, empty when offline or removed
 * @param changelog   changelog of the latest version, or null
 * @param screenshots screenshot URLs of the latest version, never null
 * @param advisory    advisory on the latest version, or null
 * @param blocked     block state of the latest version, or null
 * @param removed     registry tombstone, or null
 * @param locked      premium and no licence configured on this instance
 */
public record MarketplacePluginDetailResource(
    RegistryStatusResource registry,
    MarketplacePluginResource plugin,
    List<MarketplaceVersionResource> versions,
    MarketplaceChangelogResource changelog,
    List<String> screenshots,
    MarketplaceAdvisoryResource advisory,
    MarketplaceBlockedResource blocked,
    MarketplaceRemovedResource removed,
    boolean locked
) {

}
