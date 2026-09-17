/*
 * Copyright 2026 EPAM Systems
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
 * What this ReportPortal instance itself permits on the plugins page, as opposed to what the
 * registry offers. Separate from {@link RegistryStatusResource} because the two fail
 * independently: an unreachable registry says nothing about whether a jar may be uploaded by
 * hand, and that path is the escape valve precisely when the registry is down.
 *
 * @param uploadAllowed  whether manual .jar upload is switched on for this instance
 * @param productVersion the ReportPortal release this instance reports, or null when it does not
 *                       know — {@code rp.product.version} unset. A caller shows it to explain a
 *                       compatibility verdict ("needs 26.2 or later; this instance runs 26.1"),
 *                       and null is why no verdict can be given at all rather than a formatting
 *                       problem.
 */
public record InstanceCapabilitiesResource(boolean uploadAllowed, String productVersion) {

}
