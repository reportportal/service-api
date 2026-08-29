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

import java.time.Instant;

/**
 * One row of a plugin's version history. Ordering is not promised — the registry publishes an
 * order of its own and the page sorts.
 *
 * @param version     published version
 * @param publishedAt when the registry published it, null when it carried no date
 * @param blocked     whether the registry refuses to serve this version any more
 */
public record MarketplaceVersionResource(String version, Instant publishedAt, boolean blocked) {

}
