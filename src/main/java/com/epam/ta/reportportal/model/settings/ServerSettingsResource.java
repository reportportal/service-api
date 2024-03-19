/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.model.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Global server settings response of stored properties
 *
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class ServerSettingsResource {

  private String profile;

  private boolean active;

  @JsonProperty(value = "serverEmailConfig")
  private ServerEmailResource serverEmailResource;

  //	private Map<String, OAuthDetailsResource> oauthConfigs;

  private Map<String, AnalyticsResource> analyticsResource;

  public void setProfile(String id) {
    this.profile = id;
  }

  public String getProfile() {
    return profile;
  }

  public void setActive(boolean is) {
    this.active = is;
  }

  public boolean getActive() {
    return active;
  }

  public void setServerEmailResource(ServerEmailResource config) {
    this.serverEmailResource = config;
  }

  public ServerEmailResource getServerEmailResource() {
    return serverEmailResource;
  }

  //	public Map<String, OAuthDetailsResource> getOauthConfigs() {
  //		return oauthConfigs;
  //	}

  //	public void setOauthConfigs(Map<String, OAuthDetailsResource> oauthConfigs) {
  //		this.oauthConfigs = oauthConfigs;
  //	}

  public Map<String, AnalyticsResource> getAnalyticsResource() {
    return analyticsResource;
  }

  public void setAnalyticsResource(Map<String, AnalyticsResource> analyticsResource) {
    this.analyticsResource = analyticsResource;
  }
}
