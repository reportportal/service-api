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

import java.util.List;

/**
 * Everything the Plugins page renders, including its offline state, in one response.
 *
 * @param instance what this instance itself permits, independent of the registry
 * @param registry  registry reachability and host
 * @param installed plugins installed here, never null
 * @param available registry plugins not installed here, empty when the registry is offline
 */
public record MarketplaceCatalogueResource(
    RegistryStatusResource registry,
    InstanceCapabilitiesResource instance,
    List<InstalledPluginResource> installed,
    List<AvailablePluginResource> available
) {

}
