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

import java.nio.file.Path;

/**
 * Downloads a plugin artifact from wherever the registry pointed — the CDN URL of a public 302, or
 * the signed URL a premium response carries.
 *
 * <p>Separate from {@link MarketplaceClient} because it talks to a different host under different
 * rules: no licence header (the URL is already the credential), redirects followed, and a much
 * longer deadline, since a jar is not a JSON document.
 */
public interface MarketplaceArtifactFetcher {

  /**
   * Streams the artifact into {@code target}, replacing whatever is there.
   *
   * @param downloadUrl absolute URL of the artifact
   * @param target      file to write
   * @throws com.epam.reportportal.base.core.marketplace.exception.MarketplaceException when the
   *                                                                                    download
   *                                                                                    cannot be
   *                                                                                    completed
   */
  void fetch(String downloadUrl, Path target);
}
