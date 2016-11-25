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
 
package com.epam.ta.reportportal.ws.converter.builders;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.BinaryContent;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.ws.model.log.LogResource;

/**
 * Bulider for Log Resource
 * 
 * @author Andrei Varabyeu
 * 
 */

@Service
@Scope("prototype")
public class LogResourceBuilder extends ResourceBuilder<LogResource> {

	@Override
	protected LogResource initObject() {
		return new LogResource();
	}

	public LogResourceBuilder addLog(Log log) {
		LogResource resource = getObject();
		resource.setIdLog(log.getId());
		if(null != log.getLogMsg())
			resource.setMessage(log.getLogMsg());
		else
			resource.setMessage("NULL");
		resource.setLogTime(log.getLogTime());

		BinaryContent binaryData;
		if (null != (binaryData = log.getBinaryContent())) {
			LogResource.BinaryContent binaryContent = new LogResource.BinaryContent();
			binaryContent.setBinaryDataId(binaryData.getBinaryDataId());
			binaryContent.setContentType(binaryData.getContentType());
			binaryContent.setThumbnailId(binaryData.getThumbnailId());
			resource.setBinaryContent(binaryContent);
		}

		resource.setTestItem(log.getTestItemRef());
		if (null != log.getLevel()) {
			resource.setLevel(log.getLevel().toString());
		}
		return this;
	}

}