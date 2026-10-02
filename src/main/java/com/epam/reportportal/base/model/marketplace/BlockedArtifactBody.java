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

import java.time.Instant;

/**
 * 403 body of the artifact route when the version is blocked. {@code blocked} is boxed on purpose:
 * a licence 403 carries no such field, and only its presence tells the two 403s apart.
 */
public record BlockedArtifactBody(Boolean blocked, Instant blockedAt, String reason) {

}
