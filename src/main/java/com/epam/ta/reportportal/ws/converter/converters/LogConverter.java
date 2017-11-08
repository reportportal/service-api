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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.google.common.base.Preconditions;

import java.util.Optional;
import java.util.function.Function;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class LogConverter {

	private LogConverter() {
		//static only
	}

	public static final Function<Log, LogResource> TO_RESOURCE = model -> {
		Preconditions.checkNotNull(model);
		LogResource resource = new LogResource();
		resource.setIdLog(model.getId());
		resource.setMessage(Optional.ofNullable(model.getLogMsg()).orElse("NULL"));
		resource.setLogTime(model.getLogTime());
		Optional.ofNullable(model.getBinaryContent()).ifPresent(content -> {
			LogResource.BinaryContent binaryContent = new LogResource.BinaryContent();
			binaryContent.setBinaryDataId(content.getBinaryDataId());
			binaryContent.setContentType(content.getContentType());
			binaryContent.setThumbnailId(content.getThumbnailId());
			resource.setBinaryContent(binaryContent);
		});
		resource.setTestItem(model.getTestItemRef());
		Optional.ofNullable(model.getLevel()).ifPresent(level -> {
			resource.setLevel(level.toString());
		});
		return resource;
	};

}
