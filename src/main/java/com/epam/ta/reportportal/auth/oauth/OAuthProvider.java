/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-authorization
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.auth.oauth;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Andrei Varabyeu
 */
abstract public class OAuthProvider {

	/**
	 * Is OAuth provider support dynamic configs
	 */
	private final boolean configDynamic;

	/**
	 * Auth provider name
	 */
	private final String name;

	/**
	 * HTML code of button
	 */
	private final String button;

	public OAuthProvider(@Nonnull String name, @Nullable String button, boolean configDynamic) {
		this.name = Preconditions.checkNotNull(name, "Name should not be null");
		this.button = button;
		this.configDynamic = configDynamic;
	}

	/**
	 * Applies default settings
	 *
	 * @param details OAuth configuration
	 */
//	public void applyDefaults(ClientDetails details) {
//	}
//
//	abstract public ResourceServerTokenServices getTokenServices();
//
//	abstract public OAuth2RestOperations getOAuthRestOperations(OAuth2ClientContext context);

	public String getName() {
		return name;
	}

	public String getButton() {
		return button;
	}

	public boolean isConfigDynamic() {
		return configDynamic;
	}

	public String buildPath(String basePath) {
		return basePath + (basePath.endsWith("/") ? "" : "/") + this.name;
	}

}
