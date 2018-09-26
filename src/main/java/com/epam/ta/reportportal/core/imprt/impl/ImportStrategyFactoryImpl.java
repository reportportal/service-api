/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
