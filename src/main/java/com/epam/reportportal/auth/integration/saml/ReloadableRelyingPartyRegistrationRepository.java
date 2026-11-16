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

package com.epam.reportportal.auth.integration.saml;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.util.Assert;

/**
 * Runtime reloadable RelyingPartyRegistrationRepository
 *
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class ReloadableRelyingPartyRegistrationRepository implements
    RelyingPartyRegistrationRepository, Iterable<RelyingPartyRegistration> {

  private Map<String, RelyingPartyRegistration> byRegistrationId;

  public ReloadableRelyingPartyRegistrationRepository(RelyingPartyRegistration... registrations) {
    this(Arrays.asList(registrations));
  }

  public ReloadableRelyingPartyRegistrationRepository(
      Collection<RelyingPartyRegistration> registrations) {
    this.byRegistrationId = createMappingToIdentityProvider(registrations);
  }

  public void reloadRelyingParty(Collection<RelyingPartyRegistration> registrations) {
    byRegistrationId.clear();
    byRegistrationId = createMappingToIdentityProvider(registrations);
  }

  private Map<String, RelyingPartyRegistration> createMappingToIdentityProvider(
      Collection<RelyingPartyRegistration> rps) {
    LinkedHashMap<String, RelyingPartyRegistration> result = new LinkedHashMap<>();
    for (RelyingPartyRegistration rp : rps) {
      Assert.notNull(rp, "relying party collection cannot contain null values");
      String key = rp.getRegistrationId();
      Assert.notNull(key, "relying party identifier cannot be null");
      Assert.isNull(result.get(key),
          () -> "relying party duplicate identifier '" + key + "' detected.");
      result.put(key, rp);
    }
    return Collections.synchronizedMap(result);
  }

  @Override
  public RelyingPartyRegistration findByRegistrationId(String id) {
    return this.byRegistrationId.get(id);
  }

  @Override
  public Iterator<RelyingPartyRegistration> iterator() {
    return this.byRegistrationId.values().iterator();
  }
}
