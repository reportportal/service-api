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

package com.epam.reportportal.base.core.marketplace;

import com.epam.reportportal.base.model.marketplace.MarketplaceCompatibility;
import org.apache.commons.lang3.StringUtils;

/**
 * The ReportPortal range a version is judged by: the registry's {@code effectiveCompatibility}
 * (the author's range narrowed by any operator ceiling), or the author's {@code compatibility}
 * when the registry predates ceilings and sends no effective range.
 *
 * <p>A ceiling with no effective range is not that older registry, which never sends a ceiling.
 * It is a registry answering inconsistently, and falling back to the author's range would ignore
 * the ceiling, so the range is unknown.
 */
public final class EffectiveRange {

  private EffectiveRange() {
    // utility
  }

  /**
   * @param effective the version's {@code effectiveCompatibility}, may be null
   * @param declared  the version's {@code compatibility}, may be null
   * @param ceiling   the version's {@code compatibilityCeiling}, may be null
   * @return the range to evaluate, or null when it is unknown — not compatible
   */
  public static String of(MarketplaceCompatibility effective, MarketplaceCompatibility declared,
      String ceiling) {
    if (effective != null && StringUtils.isNotBlank(effective.reportportal())) {
      return effective.reportportal().trim();
    }
    if (StringUtils.isNotBlank(ceiling)) {
      return null;
    }
    return declared == null ? null : StringUtils.trimToNull(declared.reportportal());
  }
}
