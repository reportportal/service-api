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

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The running ReportPortal release, which is what a plugin's compatibility range is checked
 * against.
 *
 * <p>It has no default. {@code info.build.version} is the build version of this service, not the
 * product release the ranges speak about, and guessing from it would let the instance offer an
 * upgrade that does not run here. When {@code rp.product.version} is unset the version is unknown,
 * the absence is logged once at startup, and every compatibility question answers no — so no
 * update is offered rather than a wrong one.
 */
@Component
public class ProductVersion {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductVersion.class);

  private final String version;

  /**
   * Reads the release this instance runs.
   *
   * @param version the configured release, blank when the instance was never told which it runs
   */
  public ProductVersion(@Value("${rp.product.version:}") String version) {
    this.version = StringUtils.trimToNull(version);
  }

  @PostConstruct
  void reportIfUnknown() {
    if (version == null) {
      LOGGER.warn("rp.product.version is not set, so the ReportPortal release this instance runs"
          + " is unknown. Marketplace plugin compatibility cannot be decided and no plugin updates"
          + " will be offered until the property is configured.");
    }
  }

  public boolean isKnown() {
    return version != null;
  }

  /** The release this instance reports, or null when it does not know. */
  public String value() {
    return version;
  }

  /**
   * Whether a declared compatibility range covers this instance.
   *
   * <p>A range that does not parse is unknown compatibility, not compatibility: it answers no and
   * is logged, because it is a defect in what the registry published and nothing here can repair
   * it. A range that is simply absent answers no too, but silently — that is the registry saying
   * nothing rather than saying something broken.
   *
   * @param range the version detail's {@code compatibility.reportportal}, may be null
   * @return true only when the product version is known, the range parses, and it matches
   */
  public boolean satisfies(String range) {
    if (version == null) {
      return false;
    }
    var parsed = CompatibilityRange.parse(range);
    if (parsed.isEmpty()) {
      if (StringUtils.isNotBlank(range)) {
        LOGGER.warn("Marketplace compatibility range '{}' cannot be read, so it is treated as"
            + " unknown compatibility and no update is offered for it.", range);
      }
      return false;
    }
    return parsed.get().matches(version);
  }

  /**
   * The same question as {@link #satisfies(String)}, answered for a client that has to show the
   * answer rather than act on it.
   *
   * <p>{@code satisfies} folds "no" and "cannot say" together, which is right where the only
   * outcome is whether to offer something: both mean do not offer. A client drawing a row needs
   * them apart. "This version does not run here" earns a warning and a dead action; "nobody could
   * decide" must not — the causes are an instance that does not know its own release, a version
   * that declares no range, and a range that cannot be read, and marking a plugin incompatible on
   * any of those would ground every plugin on an instance that simply never set
   * {@code rp.product.version}.
   *
   * @param range a declared {@code compatibility.reportportal} range, may be null
   * @return true or false when the question can be decided, null when it cannot
   */
  public Boolean verdict(String range) {
    if (version == null || StringUtils.isBlank(range) || CompatibilityRange.parse(range).isEmpty()) {
      return null;
    }
    return satisfies(range);
  }
}
