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

package com.epam.ta.reportportal.ws.converter;

import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.converter.converters.LogConverter;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import org.springframework.stereotype.Service;

/**
 * Assembler for LogResources
 *
 * @author Andrei Varabyeu
 */
@Service
public class LogResourceAssembler extends PagedResourcesAssembler<Log, LogResource> {

	@Override
	public LogResource toResource(Log log) {
		return LogConverter.TO_RESOURCE.apply(log);
	}
}
