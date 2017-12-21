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

import com.epam.ta.reportportal.database.entity.BinaryContent;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class LogBuilder extends Builder<Log> {

	public LogBuilder addSaveLogRQ(SaveLogRQ request) {
		if (request != null) {
			Log log = getObject();
			if (null != request.getMessage()) {
				log.setLogMsg(request.getMessage());
			}
			/*
			 * Shit implementation for situations with NULL log messages 
			 */
			else {
				log.setLogMsg("NULL");
			}
			log.setLogTime(request.getLogTime());
			if (null != request.getLevel()) {
				log.setLevel(LogLevel.toLevelOrUnknown(request.getLevel()));
			}

		}
		return this;
	}

	public LogBuilder addBinaryContent(BinaryContent binaryContent) {
		getObject().setBinaryContent(binaryContent);
		return this;
	}

	public LogBuilder addTestItem(TestItem testItem) {
		if (null != testItem) {
			getObject().setTestItemRef(testItem.getId());
		}
		return this;
	}

	@Override
	protected Log initObject() {
		return new Log();
	}

}