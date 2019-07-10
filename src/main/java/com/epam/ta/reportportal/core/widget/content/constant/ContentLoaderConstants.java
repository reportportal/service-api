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

package com.epam.ta.reportportal.core.widget.content.constant;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class ContentLoaderConstants {

	public static final String CONTENT_FIELDS_DELIMITER = ",";

	public static final String RESULT = "result";
	public static final String LATEST_OPTION = "latest";
	public static final String LATEST_LAUNCH = "latestLaunch";
	public static final String LAUNCH_NAME_FIELD = "launchNameFilter";
	public static final String USER = "user";
	public static final String ACTION_TYPE = "actionType";
	public static final String ATTRIBUTES = "attributes";
	public static final String ATTRIBUTE_KEY = "attributeKey";
	public static final String PATTERN_TEMPLATE_NAME = "patternTemplateName";
	public static final String ITEM_TYPE = "type";
	public static final String INCLUDE_METHODS = "includeMethods";
	public static final String FLAKY = "flaky";
	public static final String CUSTOM_COLUMNS = "customColumns";
	public static final String TIMELINE = "timeline";

	private ContentLoaderConstants() {
		//static only
	}
}
