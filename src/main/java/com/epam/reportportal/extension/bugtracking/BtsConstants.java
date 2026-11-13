package com.epam.reportportal.extension.bugtracking;

import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import java.util.HashMap;
import java.util.Optional;

/**
 * Enum of standard Bug Tracking System configuration parameter keys used in
 * {@link com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams}.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public enum BtsConstants {
  PROJECT("project"),
  URL("url"),
  OAUTH_ACCESS_KEY("oauthAccessKey"),
  TYPE("btsType"),
  DEFECT_FORM_FIELDS("defectFormFields"),
  USER_NAME("username"),
  PASSWORD("password"),
  AUTH_TYPE("authType");

  private final String name;

  BtsConstants(String name) {
    this.name = name;
  }

  public <T> Optional<T> getParam(IntegrationParams params, Class<T> type) {
    return Optional.ofNullable(params.getParams().get(this.name)).map(type::cast);
  }

  public <T> Optional<Object> getParam(IntegrationParams params) {
    return Optional.ofNullable(params.getParams().get(this.name));
  }

  public void setParam(IntegrationParams params, Object value) {
    if (null == params.getParams()) {
      params.setParams(new HashMap<>());
    }
    params.getParams().put(this.name, value);
  }
}
