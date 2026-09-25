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

package com.epam.reportportal.base.core.marketplace.handler;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.model.marketplace.MarketplaceLicenceRQ;
import com.epam.reportportal.base.model.marketplace.MarketplaceLicenceResource;

/** Administration of this instance's marketplace licence credentials. */
public interface MarketplaceLicenceHandler {

  /**
   * What this instance holds — never the key itself.
   *
   * @return configured state and customer id
   */
  MarketplaceLicenceResource getLicence();

  /**
   * Stores credentials, replacing any that were there.
   *
   * @param request the credentials the registry operator issued
   * @param user    the admin doing it
   * @return the same answer {@link #getLicence()} now gives
   */
  MarketplaceLicenceResource setLicence(MarketplaceLicenceRQ request, ReportPortalUser user);

  /**
   * Removes the credentials, putting the instance back to premium plugins being locked. Idempotent:
   * an instance that holds none is the state this asks for, not an error.
   *
   * @param user the admin doing it
   * @return the same answer {@link #getLicence()} now gives
   */
  MarketplaceLicenceResource deleteLicence(ReportPortalUser user);
}
