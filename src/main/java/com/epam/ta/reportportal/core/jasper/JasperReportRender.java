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

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Jasper Report render based on provided JRXML template.<br>
 *
 * @author Andrei_Ramanchuk
 * @author Andrei Varabyeu
 * Performance improvements. Load JasperReport only once since it is immutable
 */
@Service("jasperRender")
class JasperReportRender {

	private static final Logger LOGGER = LoggerFactory.getLogger(JasperReportRender.class);
	private static final String REPORT_JRXML_TEMPLATE = "classpath:/templates/report/report.jrxml";

	private JasperReport jasperReport;

	@Autowired
	public JasperReportRender(ResourceLoader resourceLoader) throws JRException, IOException {
		Resource reportTemplate = resourceLoader.getResource(REPORT_JRXML_TEMPLATE);
		com.google.common.base.Preconditions.checkArgument(reportTemplate.exists());
		InputStream inputStream = reportTemplate.getInputStream();
		JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
		this.jasperReport = JasperCompileManager.compileReport(jasperDesign);

	}

	JasperPrint generateReportPrint(Map<String, Object> params, JRDataSource datasource) {
		try {
			return JasperFillManager.fillReport(jasperReport, params, datasource);
		} catch (JRException e) {
			LOGGER.error("Unable to generate Report", e);
			return new JasperPrint();
		}
	}
}