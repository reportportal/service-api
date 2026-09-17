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

package com.epam.reportportal.base.core.marketplace;

import com.epam.reportportal.base.model.marketplace.MarketplaceVersionDetail;
import com.epam.reportportal.base.model.marketplace.PluginTombstoneBody;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceAdvisoryResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceBlockedResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceRemovedResource;

/**
 * The registry's advisory, block and removal state, in the one shape the catalogue row and the
 * plugin page both render. Both read it from the same registry types, so they map it here rather
 * than twice — a badge that means one thing on a row and another on a page is the drift this
 * exists to prevent.
 */
public final class MarketplaceState {

  private MarketplaceState() {
  }

  /**
   * The advisory attached to a version, or null when the registry attached none.
   */
  public static MarketplaceAdvisoryResource advisory(MarketplaceVersionDetail version) {
    if (version == null || version.advisory() == null) {
      return null;
    }
    var advisory = version.advisory();
    return new MarketplaceAdvisoryResource(advisory.severity(), advisory.text(),
        advisory.attachedAt());
  }

  /**
   * The block state of a version, or null when it is not blocked. A version detail carries the
   * block as three loose fields; only {@code blocked} decides, the other two merely describe.
   */
  public static MarketplaceBlockedResource blocked(MarketplaceVersionDetail version) {
    if (version == null || !version.blocked()) {
      return null;
    }
    return new MarketplaceBlockedResource(version.version(), version.blockedAt(),
        version.blockReason());
  }

  /**
   * The registry's tombstone, or null when there is none.
   */
  public static MarketplaceRemovedResource removed(PluginTombstoneBody tombstone) {
    if (tombstone == null) {
      return null;
    }
    return new MarketplaceRemovedResource(tombstone.removed(), tombstone.removalReason(),
        tombstone.removedBy());
  }
}
