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
