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

import java.util.Optional;

/**
 * The opaque, stable identifier this ReportPortal instance sends with an artifact request.
 *
 * <p>It exists so the registry can tell twenty downloads by one instance from twenty instances
 * downloading once (FR-OP-09/10). That is the whole of its purpose, and it is why it carries no
 * information about the instance: a random UUID, generated once and kept, with nothing derived
 * from a URL, a licence, a customer or a user (NFR-10).
 *
 * <p>Empty means the instance has opted out. The request then goes without the header and the
 * registry counts it against a per-request anonymous id instead — the download still works, and
 * only the unique-instance figure loses that request.
 */
@FunctionalInterface
public interface MarketplaceInstanceId {

  /**
   * The identifier, or empty when analytics are switched off for this instance.
   *
   * @return the instance id
   */
  Optional<String> current();
}
