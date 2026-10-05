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
 * A security advisory the registry attached to the version running here. Null means the registry
 * attached none, never that none was looked for.
 *
 * @param severity   registry severity, e.g. {@code high}
 * @param text       what the advisory says
 * @param attachedAt when a registry operator attached it
 */
public record MarketplaceAdvisoryResource(String severity, String text, Instant attachedAt) {

}
