package com.epam.ta.reportportal.ws.resolver;

import com.epam.ta.reportportal.database.search.Queryable;
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
