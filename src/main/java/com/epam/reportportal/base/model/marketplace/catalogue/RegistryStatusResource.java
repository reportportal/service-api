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

package com.epam.reportportal.base.model.marketplace.catalogue;

/**
 * Registry reachability as of this response. The host travels with it because the offline alert
 * names the registry the operator has to go and look at.
 *
 * @param status ONLINE or OFFLINE
 * @param host   registry host that was contacted
 */
public record RegistryStatusResource(RegistryStatus status, String host) {

}
