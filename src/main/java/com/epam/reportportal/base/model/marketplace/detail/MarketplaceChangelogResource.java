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

import java.util.List;

/**
 * The changelog of one version, already split into the lines the page renders. Absent when the
 * registry publishes none for that version, or when the document could not be read.
 *
 * @param version the version the changelog belongs to
 * @param lines   non-blank lines, in the order the registry wrote them
 */
public record MarketplaceChangelogResource(String version, List<String> lines) {

}
