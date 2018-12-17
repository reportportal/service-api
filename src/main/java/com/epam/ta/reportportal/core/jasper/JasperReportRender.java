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

import static com.google.common.base.Preconditions.checkArgument;

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
		checkArgument(reportTemplate.exists());
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