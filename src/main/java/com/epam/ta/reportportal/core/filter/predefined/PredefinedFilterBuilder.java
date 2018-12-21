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

package com.epam.ta.reportportal.core.filter.predefined;

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

/**
 * @author Andrei Varabyeu
 */
public abstract class PredefinedFilterBuilder {

	public Queryable buildFilter(String[] params) {
		checkParams(params);
		return build(params);
	}

	abstract protected Queryable build(String[] params);

	protected void checkParams(String[] params) {
		//empty by default
	}

	protected Exception incorrectParamsException(String message) {
		throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, message);
	}

}
