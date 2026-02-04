/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.auth.event;

import com.epam.reportportal.auth.integration.saml.ReloadableRelyingPartyRegistrationRepository;
import com.epam.reportportal.auth.integration.saml.RelyingPartyBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.stereotype.Component;

/**
 * Handles SAML settings changes event and reload configuration of IDP in service provider configuration.
 *
 * @author Yevgeniy Svalukhin
 */
@Component
public class SamlProvidersReloadEventHandler implements
    ApplicationListener<SamlProvidersReloadEvent> {

  private final RelyingPartyBuilder relyingPartyBuilder;
  private final RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;


  public SamlProvidersReloadEventHandler(RelyingPartyBuilder relyingPartyBuilder,
      RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) {
    this.relyingPartyBuilder = relyingPartyBuilder;
    this.relyingPartyRegistrationRepository = relyingPartyRegistrationRepository;
  }

  @Override
  public void onApplicationEvent(SamlProvidersReloadEvent event) {
    if (relyingPartyRegistrationRepository instanceof ReloadableRelyingPartyRegistrationRepository reloadable) {
      reloadable.reloadRelyingParty(
          relyingPartyBuilder.createRelyingPartyRegistrations());
    }
  }
}
