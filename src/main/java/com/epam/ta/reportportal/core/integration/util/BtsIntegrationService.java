/*
 * Copyright 2021 EPAM Systems
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
package com.epam.ta.reportportal.core.integration.util;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.enums.AuthType;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.rules.exception.ErrorType;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class BtsIntegrationService extends BasicIntegrationServiceImpl {

  private final BasicTextEncryptor basicTextEncryptor;

  @Autowired
  public BtsIntegrationService(IntegrationRepository integrationRepository, PluginBox pluginBox,
      BasicTextEncryptor basicTextEncryptor) {
    super(integrationRepository, pluginBox);
    this.basicTextEncryptor = basicTextEncryptor;
  }

  @Override
  public Map<String, Object> retrieveCreateParams(String integrationType,
      Map<String, Object> integrationParams) {
    expect(integrationParams, MapUtils::isNotEmpty).verify(ErrorType.BAD_REQUEST_ERROR,
        "No integration params provided");

    Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(
        BtsProperties.values().length);

    resultParams.put(BtsProperties.PROJECT.getName(),
        BtsProperties.PROJECT.getParam(integrationParams)
            .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
                "BTS project is not specified."))
    );
    resultParams.put(BtsProperties.URL.getName(),
        BtsProperties.URL.getParam(integrationParams)
            .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
                "BTS url is not specified."))
    );

    final String authName = BtsProperties.AUTH_TYPE.getParam(integrationParams)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
            "Auth type is not specified."));
    retrieveAuthParams(integrationParams, resultParams, authName);
    resultParams.put(BtsProperties.AUTH_TYPE.getName(), authName);

    return resultParams;
  }

  @Override
  public Map<String, Object> retrieveUpdatedParams(String integrationType,
      Map<String, Object> integrationParams) {
    Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(integrationParams.size());

    BtsProperties.URL.getParam(integrationParams)
        .ifPresent(url -> resultParams.put(BtsProperties.URL.getName(), url));

    BtsProperties.PROJECT.getParam(integrationParams)
        .ifPresent(url -> resultParams.put(BtsProperties.PROJECT.getName(), url));

    BtsProperties.AUTH_TYPE.getParam(integrationParams).ifPresent(authName -> {
      retrieveAuthParams(integrationParams, resultParams, authName);
      resultParams.put(BtsProperties.AUTH_TYPE.getName(), authName);
    });

    Optional.ofNullable(integrationParams.get("defectFormFields"))
        .ifPresent(defectFormFields -> resultParams.put("defectFormFields", defectFormFields));

    return resultParams;
  }

  @Override
  public boolean checkConnection(Integration integration) {
    BtsExtension extension = pluginBox.getInstance(integration.getType().getName(),
            BtsExtension.class)
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            Suppliers.formattedSupplier("Could not find plugin with name '{}'.",
                integration.getType().getName()).get()
        ));
    expect(extension.testConnection(integration), BooleanUtils::isTrue).verify(
        ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
        "Connection refused."
    );
    return true;
  }

  /**
   * Retrieves auth params based on auth type
   */
  private Map<String, Object> retrieveAuthParams(Map<String, Object> integrationParams,
      Map<String, Object> resultParams,
      String authName) {
    AuthType authType = AuthType.findByName(authName)
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.INCORRECT_AUTHENTICATION_TYPE, authName));
    if (AuthType.BASIC.equals(authType)) {
      resultParams.put(BtsProperties.USER_NAME.getName(),
          BtsProperties.USER_NAME.getParam(integrationParams)
              .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
                  "Username value is not specified"
              ))
      );

      String encryptedPassword = basicTextEncryptor.encrypt(
          BtsProperties.PASSWORD.getParam(integrationParams)
              .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
                  "Password value is not specified")));
      resultParams.put(BtsProperties.PASSWORD.getName(), encryptedPassword);
    } else if (AuthType.OAUTH.equals(authType)) {
      final String encryptedAccessKey = basicTextEncryptor.encrypt(
          BtsProperties.OAUTH_ACCESS_KEY.getParam(integrationParams)
              .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
                  "AccessKey value is not specified")));
      resultParams.put(BtsProperties.OAUTH_ACCESS_KEY.getName(), encryptedAccessKey);
    } else {
      throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
          "Unsupported auth type for integration - " + authType.name()
      );
    }
    return resultParams;
  }

}
