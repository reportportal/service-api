/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
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

package com.epam.ta.reportportal.store.commons;

import java.util.ResourceBundle;

/**
 * Injector for global data values for initial RP items
 *
 * @author Andrei_Ramanchuk
 */
public enum Constants {
	//@formatter:off
	NONAME_USER("Users.noname"),
	
	DEFAULT_USER("Users.DefaultUser"),
	DEFAULT_USER_PASS("Users.DefaultUser.Pass"),

	USER("Users.User"),
	USER_PASS("Users.User.Pass"),
	USER_UUID("Users.User.UUID"),
	
	DEFAULT_ADMIN("Users.DefaultAdmin"),
	DEFAULT_ADMIN_PASS("Users.DefaultAdmin.Pass");
	//@formatter:on

	private final static ResourceBundle ITEMS = ResourceBundle.getBundle("init.initial-items");
	private final String ref;

	Constants(final String msgReference) {
		ref = msgReference;
	}

	@Override
	public String toString() {
		return ITEMS.getString(ref).toLowerCase();
	}
}