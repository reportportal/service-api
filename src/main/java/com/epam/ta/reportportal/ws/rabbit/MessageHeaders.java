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

package com.epam.ta.reportportal.ws.rabbit;

/**
 * @author Pavel Bortnik
 */
public final class MessageHeaders {

	public static final String REQUEST_TYPE = "requestType";
	public static final String USERNAME = "username";
	public static final String PROJECT_NAME = "projectName";
	public static final String PROJECT_ID = "projectId";
	public static final String LAUNCH_ID = "launchId";
	public static final String ITEM_ID = "itemId";
	public static final String PARENT_ITEM_ID = "parentItemId";
	public static final String XD_HEADER = "x-death";
	public static final String BASE_URL = "baseUrl";

	public static final String ITEM_REF = "itemRef";
	public static final String LIMIT = "limit";
	public static final String IS_LOAD_BINARY_DATA = "isLoadBinaryData";

	private MessageHeaders() {
		//static only
	}

}
