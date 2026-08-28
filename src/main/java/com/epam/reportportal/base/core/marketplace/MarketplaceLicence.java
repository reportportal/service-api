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

import java.util.Optional;

/**
 * Whether this instance holds credentials for premium plugins.
 *
 * <p>Asked locally and never of the registry: the registry would have to be told who is asking
 * before it could answer, and a locked badge is not worth a round trip that leaks the instance.
 * Stage 4 replaces the default answer with real encrypted storage.
 */
public interface MarketplaceLicence {

  boolean isConfigured();

  /**
   * Signs a short-lived licence JWT for one premium artifact download.
   *
   * <p>The registry keeps the URL it hands back valid for about 60 seconds, so the token is signed
   * at the moment of the download and never cached or queued.
   *
   * @param pluginId registry plugin id the token is scoped to
   * @return the token, or empty when this instance holds no licence credentials
   */
  Optional<String> signArtifactToken(String pluginId);
}
