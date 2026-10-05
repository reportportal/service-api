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
 * One version the registry refuses to serve any more. It says nothing about whether that version
 * keeps running here — it only cannot be installed or rolled back to again.
 *
 * @param version   the blocked version
 * @param blockedAt when it was blocked
 * @param reason    operator-supplied reason, null when the registry carried none
 */
public record MarketplaceBlockedResource(String version, Instant blockedAt, String reason) {

}
