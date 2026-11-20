/*
 * Copyright 2026 EPAM Systems
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
 * Whether the version installed here runs on this release, per the registry. Unlike
 * {@code compatible} on the entry, which is about the latest version, this is about the code that
 * is running.
 *
 * @param compatible    false when the version does not support this release; null when that cannot
 *                      be decided. A caller warns only on an explicit false.
 * @param requires      the range the verdict was made against, for display only
 * @param ceiling       the operator's upper bound on the version, or null
 * @param ceilingReason the operator's note on the ceiling, or null
 */
public record InstalledCompatibilityResource(
    Boolean compatible,
    String requires,
    String ceiling,
    String ceilingReason
) {

}
