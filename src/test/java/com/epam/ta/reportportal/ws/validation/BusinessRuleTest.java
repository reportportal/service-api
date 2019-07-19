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


package com.epam.ta.reportportal.ws.validation;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.BusinessRuleViolationException;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for business rule logic
 *
 * @author Andrei Varabyeu
 */
class BusinessRuleTest {

	@Test
	void testVerifyCustomError() {
		assertThrows(
				ReportPortalException.class,
				() -> BusinessRule.expect("", Predicates.isNull()).verify(ErrorType.FINISH_TIME_EARLIER_THAN_START_TIME, "")
		);
	}

	@Test
	void testVerifyBusinessError() {
		assertThrows(
				BusinessRuleViolationException.class,
				() -> BusinessRule.expect("", Predicates.alwaysFalse(), Suppliers.stringSupplier("error")).verify()
		);
	}
}