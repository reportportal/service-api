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
 * Catalogue entry of GET /api/v1/plugins.
 *
 * <p>{@code category}/{@code access}/{@code tier} stay Strings so a new registry value cannot break
 * deserialization.
 *
 * <p>{@code contactUrl} is a manifest field the listing carries too. Reading it here rather than
 * from plugin detail is what keeps the catalogue at one registry request: a page full of premium
 * entries would otherwise cost one detail fetch each just to learn where "get in touch" points.
 *
 * <p>{@code compatibility} is the range declared by {@code latestVersion} — that version's range,
 * not the plugin's, which is only meaningful because the entry names which version it means. It is
 * here for the same reason as {@code contactUrl}: a catalogue row states whether the build it
 * offers runs on this instance, and reading that from version detail would cost one fetch per row.
 * Null for a plugin whose latest version predates the field in the registry, and null is undecided
 * rather than compatible.
 */
public record MarketplacePlugin(
    String id,
    String name,
    String latestVersion,
    String description,
    String category,
    String access,
    String tier,
    String contactUrl,
    MarketplaceAuthor author,
    String compatibility
) {

}
