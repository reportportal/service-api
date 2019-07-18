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

package com.epam.ta.reportportal.ws.controller.constants;

/**
 * @author <a href="mailto:tatyana_gladysheva@epam.com">Tatyana Gladysheva</a>
 */
public final class ValidationTestsConstants {

	public static final String ID_PATH = "/555";

	public static final String INCORRECT_REQUEST_MESSAGE = "Incorrect Request. ";
	public static final String FIELD_NAME_IS_NULL_MESSAGE = "[Field 'name' should not be null.] ";
	public static final String FIELD_NAME_IS_BLANK_MESSAGE = "Field 'name' should not contain only white spaces and shouldn't be empty.";
	public static final String FIELD_NAME_SIZE_MESSAGE = "Field 'name' should have size from '3' to '128'.";

	private ValidationTestsConstants() {}
}
