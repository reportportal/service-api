package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorRS;

import java.util.function.Function;

public class ExceptionConverter {

	private ExceptionConverter() {
		//static only
	}

	public static final Function<ReportPortalException, ErrorRS> TO_ERROR_RS = ex -> {
		ErrorRS errorResponse = new ErrorRS();
		errorResponse.setErrorType(ex.getErrorType());
		errorResponse.setMessage(ex.getMessage());
		return errorResponse;
	};
}
