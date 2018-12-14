/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.util;

import com.epam.ta.reportportal.ws.model.ItemAttributeResource;

import java.util.Set;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class AttributesValidator {

	private AttributesValidator() {
		//static only
	}

	public static void validateAttributes(Set<ItemAttributeResource> attributes) {
		if (attributes != null && !attributes.isEmpty()) {
			attributes.forEach(it -> expect(it.isSystem(), Predicate.isEqual(false)).verify(BAD_REQUEST_ERROR,
					"System attributes is not applicable here"
			));
		}
	}
}
