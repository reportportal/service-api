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

package com.epam.reportportal.base.model.marketplace;

/**
 * What an admin may learn about this instance's licence credentials: whether it has any, and who
 * it signs as.
 *
 * <p>There is deliberately no field for the private key, and there must never be one. It goes in
 * and is never read back out — an operator who has lost it asks the registry for a new one.
 *
 * @param configured whether this instance holds licence credentials
 * @param customerId the configured customer id, null when there are none
 */
public record MarketplaceLicenceResource(boolean configured, String customerId) {

}
