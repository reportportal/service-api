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
 * <p>{@code advisory} and {@code blocked} are about the version installed here, not about the
 * latest one: they are the row's badges, and a badge on an installed row is a statement about the
 * code this instance is running. {@code removed} is about the plugin as a whole.
 *
 * <p>{@code name} and {@code description} are the registry's, and they are here because a local
 * row often has neither: a PF4J plugin is identified by an id like {@code jira}, and that is what
 * the plugins list would otherwise have to print. They are null for a plugin known only by its
 * tombstone — a removed plugin has no catalogue entry left to read them from.
 *
 * @param pluginId        registry plugin id
 * @param name            display name in the registry, or null
 * @param description     one-line description in the registry, or null
 * @param access          {@code public} or {@code premium}
 * @param tier            trust tier, e.g. {@code official}
 * @param latestVersion   latest version the registry publishes
 * @param updateAvailable newer, compatible, unblocked version, or null
 * @param advisory        advisory on the installed version, or null
 * @param blocked         block state of the installed version, or null
 * @param removed         registry tombstone, or null
 * @param locked          premium and no licence configured on this instance
 */
public record MarketplaceEntryResource(
    String pluginId,
    String name,
    String description,
    String access,
    String tier,
    String latestVersion,
    UpdateAvailableResource updateAvailable,
    MarketplaceAdvisoryResource advisory,
    MarketplaceBlockedResource blocked,
    MarketplaceRemovedResource removed,
    boolean locked
) {

}
