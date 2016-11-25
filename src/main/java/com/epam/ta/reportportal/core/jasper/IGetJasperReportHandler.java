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

import net.sf.jasperreports.engine.JasperPrint;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Get export reports utilities
 *
 * @author Andrei_Ramanchuk
 */
public interface IGetJasperReportHandler {

	/**
	 * Generate rendered report representation.
	 *
	 * @param launchId
	 * @param username
	 * @return JasperPrint
	 */
	JasperPrint getLaunchDetails(String launchId, String username);

	/**
	 * Finds report format and checks whether it's valid
	 *
	 * @param format ReportFormat
	 */
	ReportFormat getReportFormat(String format);

	/**
	 * Convert rendered report to output stream.
	 *
	 * @param format       Report format
	 * @param outputStream Stream report should be written to
	 * @param jasperPrint  Report Data
	 * @throws IOException In case of IO error
	 */
	void writeReport(ReportFormat format, OutputStream outputStream, JasperPrint jasperPrint) throws IOException;
}