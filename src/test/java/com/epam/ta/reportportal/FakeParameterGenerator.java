/*
 *
 *  * Copyright (C) 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal;

import com.epam.ta.reportportal.store.database.entity.item.Parameter;

/**
 * @author Dzianis_Shybeka
 */
public class FakeParameterGenerator implements FakeDataGenerator<Parameter> {

	static final String TEST_PARAM_KEY = "param key";
	static final String TEST_PARAM_VALUE = "param value";

	private String key = TEST_PARAM_KEY;
	private String value = TEST_PARAM_VALUE;

	private final Parameter parameter;

	public FakeParameterGenerator() {

		parameter = new Parameter();
	}

	public Parameter generate() {

		parameter.setKey(key);
		parameter.setValue(value);

		return parameter;
	}
}
