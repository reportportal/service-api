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

import com.epam.reportportal.base.model.marketplace.MarketplaceAdvisory;
import java.time.Instant;

/**
 * One row of a plugin's version history, which is one row of the versions table. Ordering is not
 * promised — the registry publishes an order of its own and the page sorts.
 *
 * @param version     published version
 * @param publishedAt when the registry published it, null when it carried no date
 * @param blocked     whether the registry refuses to serve this version any more
 * @param compatible whether this version runs on the release this instance reports. {@code null}
 *                   is the third answer and not a missing one: the version declares no range, or
 *                   the range cannot be read, or the instance does not know its own release. A
 *                   caller must refuse rather than guess, which is exactly what the install
 *                   handler already does with the same three cases.
 * @param requires   the range this version declares, verbatim and for display only — "&gt;=26.2".
 *                   A caller shows it to explain the verdict ("needs 26.2 or later, this instance
 *                   runs 26.1") and must not parse it to decide anything: {@code compatible} is
 *                   the decision, made here, and a second reading of the range on the other side
 *                   would be a second chance to disagree with the refusal that follows.
 * @param advisory   the advisory attached to this version, or null. Per version, unlike the
 *                   plugin-level advisory, which answers whether the plugin as a whole is under
 *                   one.
 */
public record MarketplaceVersionResource(
    String version,
    Instant publishedAt,
    boolean blocked,
    Boolean compatible,
    String requires,
    MarketplaceAdvisory advisory
) {

}
