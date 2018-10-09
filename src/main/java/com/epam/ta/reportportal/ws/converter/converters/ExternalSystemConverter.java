/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.ws.model.externalsystem.ExternalSystemResource;
import com.google.common.base.Preconditions;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts internal DB model from/to DTO
 *
 * @author Pavel Bortnik
 */
public final class ExternalSystemConverter {

	private ExternalSystemConverter() {
		//static only
	}

	public static final Function<BugTrackingSystem, ExternalSystemResource> TO_RESOURCE = bugTrackingSystem -> {
		Preconditions.checkNotNull(bugTrackingSystem);
		ExternalSystemResource resource = new ExternalSystemResource();
		resource.setSystemId(bugTrackingSystem.getId().toString());
		resource.setUrl(bugTrackingSystem.getUrl());
		resource.setProjectRef(bugTrackingSystem.getProject().getId().toString());
		resource.setProject(bugTrackingSystem.getBtsProject());
		resource.setExternalSystemType(bugTrackingSystem.getBtsType());
		resource.setFields(bugTrackingSystem.getDefectFormFields()
				.stream()
				.map(ExternalSystemFieldsConverter.FIELD_TO_MODEL)
				.collect(Collectors.toList()));
		return resource;
	};
}
