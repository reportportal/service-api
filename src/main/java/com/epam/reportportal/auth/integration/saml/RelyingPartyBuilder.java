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

import com.epam.reportportal.auth.integration.parameter.SamlParameter;
import com.epam.reportportal.auth.util.CertificationUtil;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.stereotype.Component;

/**
 * Builds {@link RelyingPartyRegistration} list from SAML providers stored in DB. Each provider should have at least IDP
 * metadata URL and IDP name specified.
 *
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Slf4j
@Component
public class RelyingPartyBuilder {

  @Value("${rp.auth.saml.entity-id}")
  private String entityId;

  @Value("${rp.auth.saml.key-alias}")
  private String keyAlias;

  @Value("${rp.auth.saml.key-password}")
  private String keyPassword;

  @Value("${rp.auth.saml.key-store}")
  private String keyStore;

  @Value("${rp.auth.saml.key-store-password}")
  private String keyStorePassword;

  @Value("${rp.auth.saml.signed-requests}")
  private Boolean signedRequests;

  private static final String CALL_BACK_URL = "{baseUrl}/login/saml2/sso/{registrationId}";

  private static final String SAML_TYPE = "saml";

  private final IntegrationRepository integrationRepository;

  private final IntegrationTypeRepository integrationTypeRepository;

  /**
   * Constructor with dependencies.
   *
   * @param integrationRepository     Integration repository
   * @param integrationTypeRepository Integration type repository
   */
  public RelyingPartyBuilder(IntegrationRepository integrationRepository,
      IntegrationTypeRepository integrationTypeRepository) {
    this.integrationRepository = integrationRepository;
    this.integrationTypeRepository = integrationTypeRepository;
  }

  /**
   * Creates list of {@link RelyingPartyRegistration} from SAML providers stored in DB. Each provider should have at
   * least IDP metadata URL and IDP name specified.
   *
   * @return List of {@link RelyingPartyRegistration}
   */
  public List<RelyingPartyRegistration> createRelyingPartyRegistrations() {
    var samlIntegrationType = integrationTypeRepository.findByName(SAML_TYPE)
        .orElseThrow(() -> new RuntimeException("SAML Integration Type not found"));

    var providers = integrationRepository.findAllGlobalByType(samlIntegrationType);

    var registrations = providers.stream()
        .flatMap(provider -> {
          try {
            var metadataLocation = SamlParameter.IDP_METADATA_URL.getParameter(provider)
                .orElseThrow(() -> new IllegalStateException("IDP metadata URL is missing"));

            var registrationId = SamlParameter.IDP_NAME.getParameter(provider)
                .orElseThrow(() -> new IllegalStateException("IDP name is missing"));

            var registration = RelyingPartyRegistrations.fromMetadataLocation(metadataLocation)
                .registrationId(registrationId)
                .assertionConsumerServiceLocation(CALL_BACK_URL)
                .entityId(entityId)
                .signingX509Credentials((c) -> {
                  if (Boolean.TRUE.equals(signedRequests)) {
                    c.add(getSigningCredential());
                  }
                })
                .build();
            return Stream.of(registration);
          } catch (Exception e) {
            log.warn("Skipping SAML provider due to metadata error: {}", e.getMessage());
            return Stream.empty();
          }
        })
        .toList();

    if (registrations.isEmpty()) {
      log.warn("No valid SAML providers registered. SAML login will be unavailable.");
    }

    return registrations;
  }

  private Saml2X509Credential getSigningCredential() {
    X509Certificate certificate = CertificationUtil.getCertificateByName(keyAlias, keyStore, keyStorePassword);
    PrivateKey privateKey = CertificationUtil.getPrivateKey(keyAlias, keyPassword, keyStore, keyStorePassword);
    return new Saml2X509Credential(privateKey, certificate, Saml2X509Credential.Saml2X509CredentialType.SIGNING);
  }
}
