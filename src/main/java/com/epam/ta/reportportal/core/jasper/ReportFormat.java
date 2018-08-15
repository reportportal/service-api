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
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.jasper;

import com.google.common.net.MediaType;

import java.util.Arrays;
import java.util.Optional;

/**
 * Supported Jasper Report formats
 *
 * @author Andrei_Ramanchuk
 * @author Andrei Varabyeu
 * Make content type part of ReportFormat enum
 */
public enum ReportFormat {
	//@formatter:off
	XLS("xls", MediaType.MICROSOFT_EXCEL.withoutParameters().toString()),
	HTML("html", MediaType.HTML_UTF_8.withoutParameters().toString()),
	PDF("pdf", MediaType.PDF.withoutParameters().toString());
	//@formatter:on

	private String value;
	private String contentType;

	ReportFormat(String value, String contentType) {
		this.value = value;
		this.contentType = contentType;
	}

	public String getValue() {
		return value;
	}

	public String getContentType() {
		return contentType;
	}

	public static Optional<ReportFormat> findByName(String name) {
		return Arrays.stream(values()).filter(format -> format.name().equalsIgnoreCase(name)).findAny();
	}
}
