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

package com.epam.ta.reportportal.ws.rabbit;

/**
 * @author Pavel Bortnik
 */
public final class MessageHeaders {

	public static final String USERNAME = "username";
	public static final String PROJECT_NAME = "projectName";
	public static final String LAUNCH_ID = "launchId";
	public static final String ITEM_ID = "itemId";
	public static final String LOG_ITEM_ID = "itemRef";
	public static final String LIMIT = "limit";
	public static final String IS_LOAD_BINARY_DATA = "isLoadBinaryData";

	private MessageHeaders() {
		//static only
	}

}
