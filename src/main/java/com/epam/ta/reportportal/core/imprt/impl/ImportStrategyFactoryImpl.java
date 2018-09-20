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
package com.epam.ta.reportportal.core.imprt.impl;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.epam.ta.reportportal.core.imprt.FileExtensionConstant.XML_EXTENSION;
import static com.epam.ta.reportportal.core.imprt.FileExtensionConstant.ZIP_EXTENSION;

@Service
@Configuration
public class ImportStrategyFactoryImpl implements ImportStrategyFactory {

	private final Map<ImportType, Map<String, ImportStrategy>> MAPPING;

	@Autowired
	public ImportStrategyFactoryImpl(ImportStrategy zipImportStrategy, ImportStrategy xmlImportStrategy) {
		Map<String, ImportStrategy> xunitStrategyMap = ImmutableMap.<String, ImportStrategy>builder().put(ZIP_EXTENSION, zipImportStrategy)
				.put(XML_EXTENSION, xmlImportStrategy)
				.build();
		MAPPING = ImmutableMap.<ImportType, Map<String, ImportStrategy>>builder().put(ImportType.XUNIT, xunitStrategyMap).build();
	}

	@Override
	public ImportStrategy getImportStrategy(ImportType type, String filename) {
		return MAPPING.get(type).get(FilenameUtils.getExtension(filename));
	}
}
