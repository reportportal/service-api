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
 * @param compatible    whether {@code latestVersion} runs on the release this instance reports.
 *                      {@code null} is the third answer and not a missing one — the version
 *                      declares no range, the range cannot be read, or the instance does not know
 *                      its own release. A row must not mark a plugin incompatible on any of those:
 *                      an instance that never set {@code rp.product.version} would otherwise show
 *                      every plugin as unusable.
 * @param requires      the range {@code latestVersion} declares, verbatim and for display only —
 *                      "&gt;=26.2". A caller shows it to explain the verdict and must not parse it
 *                      to decide anything; {@code compatible} is the decision, made here.
 * @param locked        premium and no licence configured on this instance
 */
public record AvailablePluginResource(
    String id,
    String name,
    String latestVersion,
    String description,
    String author,
    String contactUrl,
    String groupType,
    String access,
    String tier,
    Boolean compatible,
    String requires,
    boolean locked
) {

}
