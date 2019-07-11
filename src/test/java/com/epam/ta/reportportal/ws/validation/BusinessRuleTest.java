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