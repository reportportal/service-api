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

import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceCatalogueResource;

/**
 * Builds the merged catalogue the Plugins page renders from — installed plugins, registry plugins
 * that are not installed, and the registry's reachability — in one answer.
 */
public interface GetMarketplaceCatalogueHandler {

  /**
   * Reads the merged catalogue.
   *
   * @param q        free-text filter, applied to both groups, may be null or blank
   * @param category registry category filter, applied to both groups, may be null or blank
   * @return the catalogue; never throws because the registry is down
   */
  MarketplaceCatalogueResource getCatalogue(String q, String category);
}
