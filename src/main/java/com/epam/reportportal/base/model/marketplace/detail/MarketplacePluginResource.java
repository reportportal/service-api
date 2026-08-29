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

/**
 * The plugin the page is about, as the registry publishes it. Null on the envelope when the
 * registry could not be asked, which is the one thing the flat shape could not say.
 *
 * <p>A removed plugin keeps its {@code id} and loses everything else: the registry serves a
 * tombstone rather than a manifest, and the id is the only field that survives it. The page still
 * needs the id to know which plugin it is looking at.
 *
 * @param id            registry plugin id
 * @param name          display name, null for a removed plugin
 * @param description   short description, null for a removed plugin
 * @param latestVersion latest published version, null for a removed plugin
 * @param access        {@code public} or {@code premium}, null for a removed plugin
 * @param tier          trust tier, null for a removed plugin
 */
public record MarketplacePluginResource(
    String id,
    String name,
    String description,
    String latestVersion,
    String access,
    String tier
) {

}
