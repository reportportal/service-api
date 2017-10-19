/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.auth;

import com.epam.ta.reportportal.database.entity.user.User;

import java.util.Date;

/**
 * Expiration policy for ReportPortal Users
 *
 * @author Andrei Varabyeu
 */
public interface ExpirationPolicy {

	/**
	 * Returns date when account should be expired according to policy
	 * implementation
	 *
	 * @return
	 */
	Date getExpirationDate();

	/**
	 * Check whether user account should be expired or not
	 *
	 * @param user
	 * @return
	 */
	boolean isExpired(User user);

}