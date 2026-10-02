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

/**
 * What is now active after an install, update or rollback.
 *
 * @param integrationTypeId local integration type the plugin runs as
 * @param name              PF4J plugin id, which is the integration type name
 * @param pluginId          registry plugin id, persisted into the integration type details
 * @param version           registry version now installed
 */
public record MarketplaceInstallResource(Long integrationTypeId, String name, String pluginId,
                                         String version) {

}
