
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

import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;

import java.io.OutputStream;
import java.util.Map;

/**
 * Get export reports utilities
 *
 * @author Andrei_Ramanchuk
 */
public interface GetJasperReportHandler<T> {

	/**
	 * Generate rendered report representation.
	 *
	 * @param params     Parameters for Jasper view
	 * @param dataSource Data for Jasper view
	 * @return {@link JasperPrint}
	 */
	JasperPrint getJasperPrint(Map<String, Object> params, JRDataSource dataSource);

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
	 */
	void writeReport(ReportFormat format, OutputStream outputStream, JasperPrint jasperPrint);

	/**
	 * Convert entity parameters for {@link JasperPrint} creation
	 *
	 * @param entity Entity for report parameters retrieving
	 * @return {@link Map} with Jasper column name as KEY and Launch parameter as VALUE
	 */
	Map<String, Object> convertParams(T entity);
}