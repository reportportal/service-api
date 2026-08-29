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

import com.epam.reportportal.base.model.marketplace.detail.MarketplacePluginDetailResource;

/**
 * Builds the marketplace half of one plugin's page: what the registry publishes about it, and
 * nothing about what is installed here.
 *
 * <p>The answer carries the catalogue's registry envelope, so "the registry could not be reached"
 * is a state of the page rather than a failure to produce one.
 */
public interface GetMarketplacePluginDetailHandler {

  /**
   * Reads one plugin's registry detail.
   *
   * @param registryId registry plugin id
   * @return the registry's view of that plugin, removed and offline state included
   */
  MarketplacePluginDetailResource getPluginDetail(String registryId);
}
