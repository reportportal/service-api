/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.store.database;

import java.io.InputStream;

/**
 * Binary data representation. Contains only input stream and data type.
 * Introduced to simplify store/retrieve operations
 *
 * @author Andrei Varabyeu
 */
public class BinaryData {

	/**
	 * MIME Type of Binary Data
	 */
	private final String contentType;

	/**
	 * Data Stream
	 */
	private final InputStream inputStream;

	/**
	 * Content length
	 */
	private Long length;

	public BinaryData(String contentType, Long length, InputStream inputStream) {
		this.contentType = contentType;
		this.length = length;
		this.inputStream = inputStream;
	}

	public String getContentType() {
		return contentType;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public Long getLength() {
		return length;
	}

}