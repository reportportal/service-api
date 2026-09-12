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

import com.epam.reportportal.base.core.marketplace.Ed25519LicenceKey;
import com.epam.reportportal.base.core.marketplace.MarketplaceLicenceStore;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.marketplace.MarketplaceLicenceRQ;
import com.epam.reportportal.base.model.marketplace.MarketplaceLicenceResource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Sets and reports the licence credentials.
 *
 * <p>The key is parsed before it is stored. An operator who pasted half a key finds out here,
 * while they are still looking at the form, rather than from a premium install that fails days
 * later at the registry with an error that cannot tell them why.
 */
@Service
public class MarketplaceLicenceHandlerImpl implements MarketplaceLicenceHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MarketplaceLicenceHandlerImpl.class);

  private final MarketplaceLicenceStore store;

  public MarketplaceLicenceHandlerImpl(MarketplaceLicenceStore store) {
    this.store = store;
  }

  @Override
  public MarketplaceLicenceResource getLicence() {
    return store.customerId()
        .map(customerId -> new MarketplaceLicenceResource(true, customerId))
        .orElseGet(() -> new MarketplaceLicenceResource(false, null));
  }

  @Override
  public MarketplaceLicenceResource setLicence(MarketplaceLicenceRQ request,
      ReportPortalUser user) {
    var customerId = StringUtils.trimToEmpty(request.customerId());
    var privateKey = StringUtils.trimToEmpty(request.privateKey());
    try {
      Ed25519LicenceKey.privateKey(privateKey);
    } catch (IllegalArgumentException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
    }
    store.save(customerId, privateKey);
    // The customer id is the whole of what may be said about this; the key is never logged.
    LOGGER.info("Marketplace licence credentials for customer '{}' set by '{}'", customerId,
        user.getUsername());
    return new MarketplaceLicenceResource(true, customerId);
  }

  @Override
  public MarketplaceLicenceResource deleteLicence(ReportPortalUser user) {
    store.clear();
    LOGGER.info("Marketplace licence credentials removed by '{}'", user.getUsername());
    return new MarketplaceLicenceResource(false, null);
  }
}
