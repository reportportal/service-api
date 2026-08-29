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

import java.time.Instant;

/**
 * The registry's tombstone for a plugin it no longer publishes. Present means removed from the
 * marketplace, not gone from this instance: the plugin keeps running, but no version of it can be
 * installed, updated or rolled back to again.
 *
 * @param removed       when it was removed
 * @param removalReason operator-supplied reason, null when the tombstone carried none
 * @param removedBy     operator who removed it, null when unknown
 */
public record MarketplaceRemovedResource(Instant removed, String removalReason, String removedBy) {

}
