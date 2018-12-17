
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

import com.epam.ta.reportportal.auth.ReportPortalUser;
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
	 * @param user
	 * @return JasperPrint
	 */
	JasperPrint getLaunchDetails(Long launchId, ReportPortalUser user);

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