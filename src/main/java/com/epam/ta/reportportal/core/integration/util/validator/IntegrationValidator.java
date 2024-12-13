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

package com.epam.ta.reportportal.core.integration.util.validator;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.model.integration.IntegrationRQ;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class IntegrationValidator {

  private static final String RALLY_BASE_URL = "https://rally1.rallydev.com";

  private static final String JIRA_URL_PATTERN = "https://[^?]*\\.atlassian\\.(com|net).*";

  private IntegrationValidator() {
    //static only
  }

  /**
   * Validation fails if a project has at least one integration with the same type as the provided
   * global integration has
   *
   * @param project     {@link Project}
   * @param integration {@link Integration} with {@link Integration#project == NULL}
   */
  public static void validateProjectLevelIntegrationConstraints(Project project,
      Integration integration) {

    BusinessRule.expect(integration.getProject(), Objects::isNull)
        .verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            Suppliers.formattedSupplier("Integration with ID = '{}' is not global.",
                integration.getId())
        );

    BusinessRule.expect(
            project.getIntegrations().stream().map(Integration::getType).noneMatch(it -> {
              IntegrationType integrationType = integration.getType();
              return it.getIntegrationGroup() == integrationType.getIntegrationGroup()
                  && StringUtils.equalsIgnoreCase(it.getName(),
                  integrationType.getName());
            }), equalTo(true))
        .verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            Suppliers.formattedSupplier(
                "Global integration with ID = '{}' has been found, but you cannot use it, because you have project-level integration(s) of that type",
                integration.getId()
            ).get()
        );
  }


  /**
   * Validates Validates Rally and Jira server urls
   *
   * @param integrationRq {@link IntegrationRQ}
   * @param type          {@link Integration} with {@link IntegrationType}
   */
  public static void validateThirdPartyUrl(IntegrationRQ integrationRq, IntegrationType type) {
    var valid = switch (type.getName()) {
      case "rally" -> integrationRq.getIntegrationParams()
          .get("url").toString()
          .startsWith(RALLY_BASE_URL);
      case "JIRA Cloud" -> Pattern.matches(JIRA_URL_PATTERN,
          integrationRq.getIntegrationParams().get("url").toString());
      default -> true;
    };
    BusinessRule.expect(valid, Predicates.equalTo(true))
        .verify(ErrorType.BAD_REQUEST_ERROR,
            Suppliers.formattedSupplier("Integration url is not acceptable")
        );
  }
}
