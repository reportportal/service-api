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

import org.springframework.stereotype.Component;

/**
 * Stands in until licence storage exists. Answering "not configured" makes every premium plugin
 * show as locked, which is the truthful state of an instance that cannot yet hold a licence.
 */
@Component
public class UnconfiguredMarketplaceLicence implements MarketplaceLicence {

  @Override
  public boolean isConfigured() {
    return false;
  }
}
