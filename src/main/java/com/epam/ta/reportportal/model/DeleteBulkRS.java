/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.model;

import com.epam.reportportal.rules.exception.ErrorRS;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class DeleteBulkRS {

	@JsonProperty("successfullyDeleted")
	private List<Long> deleted;

	@JsonProperty("notFound")
	private List<Long> notFound;

	@JsonProperty("errors")
	private List<ErrorRS> errors;

	public DeleteBulkRS() {
	}

	public DeleteBulkRS(List<Long> deleted, List<Long> notFound, List<ErrorRS> errors) {
		this.deleted = deleted;
		this.notFound = notFound;
		this.errors = errors;
	}

	public List<Long> getDeleted() {
		return deleted;
	}

	public void setDeleted(List<Long> deleted) {
		this.deleted = deleted;
	}

	public List<Long> getNotFound() {
		return notFound;
	}

	public void setNotFound(List<Long> notFound) {
		this.notFound = notFound;
	}

	public List<ErrorRS> getErrors() {
		return errors;
	}

	public void setErrors(List<ErrorRS> errors) {
		this.errors = errors;
	}

	@Override
	public String toString() {
		return "DeleteBulkRS{" + "deleted=" + deleted + ", notFound=" + notFound + ", errors=" + errors + '}';
	}
}
