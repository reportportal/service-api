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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.store.database.entity.log.Log;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Pavel Bortnik
 */
public class LogBuilder implements Supplier<Log> {

	private Log log;

	public LogBuilder() {
		log = new Log();
	}

	public LogBuilder addSaveLogRq(SaveLogRQ createLogRQ) {
		log.setLogLevel(Integer.parseInt(createLogRQ.getLevel()));
		log.setLogMessage(Optional.ofNullable(createLogRQ.getMessage()).orElse("NULL"));
		log.setLogTime(new Timestamp(createLogRQ.getLogTime().getTime()));
		return this;
	}

	@Override
	public Log get() {
		return log;
	}

}
