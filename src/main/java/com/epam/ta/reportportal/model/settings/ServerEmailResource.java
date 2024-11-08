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
import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;

/**
 * Configurable email setting for project object
 *
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class ServerEmailResource implements Serializable {

	/**
	 * Generated sUID
	 */
	private static final long serialVersionUID = 2573744596368345366L;

	private boolean enabled = true;

	@NotBlank
	private String host;

	private Integer port;

	private String protocol;

	private Boolean authEnabled;

	private Boolean starTlsEnabled;

	private Boolean sslEnabled;

	private String username;

	private String password;

	private String from;

	public ServerEmailResource() {
	}

	public ServerEmailResource(Boolean enabled, String host, Integer port, String protocol, Boolean authEnabled, Boolean starTlsEnabled,
			Boolean sslEnabled, String username, String password, String from) {
		this.enabled = enabled;
		this.host = host;
		this.port = port;
		this.protocol = protocol;
		this.authEnabled = authEnabled;
		this.starTlsEnabled = starTlsEnabled;
		this.sslEnabled = sslEnabled;
		this.username = username;
		this.password = password;
		this.from = from;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public Boolean getAuthEnabled() {
		return authEnabled;
	}

	public void setAuthEnabled(Boolean authEnabled) {
		this.authEnabled = authEnabled;
	}

	public Boolean getStarTlsEnabled() {
		return starTlsEnabled;
	}

	public void setStarTlsEnabled(Boolean starTlsEnabled) {
		this.starTlsEnabled = starTlsEnabled;
	}

	public Boolean getSslEnabled() {
		return sslEnabled;
	}

	public void setSslEnabled(Boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ServerEmailResource{");
		sb.append("host='").append(host).append('\'');
		sb.append(", port=").append(port);
		sb.append(", protocol='").append(protocol).append('\'');
		sb.append(", authEnabled=").append(authEnabled);
		sb.append(", starTlsEnabled=").append(starTlsEnabled);
		sb.append(", sslEnabled=").append(sslEnabled);
		sb.append(", username='").append(username).append('\'');
		sb.append(", password='").append(password).append('\'');
		sb.append(", from='").append(from).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
