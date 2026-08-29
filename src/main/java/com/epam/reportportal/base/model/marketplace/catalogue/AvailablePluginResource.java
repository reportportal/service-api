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
 * A registry plugin that is not installed here.
 *
 * @param id            registry plugin id
 * @param name          display name
 * @param latestVersion latest published version
 * @param description   short description
 * @param contactUrl    where a "get in touch" enquiry goes, null when the manifest carried none
 * @param groupType     integration group the registry category maps to, null when unknown
 * @param access        {@code public} or {@code premium}
 * @param tier          trust tier
 * @param locked        premium and no licence configured on this instance
 */
public record AvailablePluginResource(
    String id,
    String name,
    String latestVersion,
    String description,
    String contactUrl,
    String groupType,
    String access,
    String tier,
    boolean locked
) {

}
