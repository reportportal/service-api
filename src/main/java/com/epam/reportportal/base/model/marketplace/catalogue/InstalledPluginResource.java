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
 * A plugin installed on this instance, with the registry's view of it when one could be matched.
 *
 * @param integrationTypeId local integration type id
 * @param name              PF4J plugin id, which is what the UI keys on today
 * @param version           installed version, null when the details blob carries none
 * @param enabled           whether the integration type is enabled
 * @param groupType         integration group, e.g. BTS
 * @param marketplace       registry block, or null when offline or unmatched
 */
public record InstalledPluginResource(
    Long integrationTypeId,
    String name,
    String version,
    boolean enabled,
    String groupType,
    MarketplaceEntryResource marketplace
) {

}
