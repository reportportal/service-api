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

package com.epam.ta.reportportal.exception;

/**
 * Exceptions related to data storage
 *
 * @author Andrei Varabyeu
 */
// TODO add binding to this exception
public class DataStorageException extends ReportPortalException {

	private static final long serialVersionUID = -6822780391660931103L;

	public DataStorageException(String message) {
		super(message);

	}

	public DataStorageException(String message, Throwable e) {
		super(message, e);
	}

}