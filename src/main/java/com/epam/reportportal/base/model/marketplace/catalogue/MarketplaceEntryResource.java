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

package com.epam.reportportal.base.model.marketplace.catalogue;

/**
 * What the registry says about an installed plugin. Null on an installed row when the registry is
 * offline or the plugin could not be matched — the UI renders both the same way, because in both
 * cases the user is equally unable to know.
 *
 * @param pluginId        registry plugin id
 * @param access          {@code public} or {@code premium}
 * @param tier            trust tier, e.g. {@code official}
 * @param latestVersion   latest version the registry publishes
 * @param updateAvailable newer, compatible, unblocked version, or null
 * @param locked          premium and no licence configured on this instance
 */
public record MarketplaceEntryResource(
    String pluginId,
    String access,
    String tier,
    String latestVersion,
    UpdateAvailableResource updateAvailable,
    boolean locked
) {

}
