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

package com.epam.ta.reportportal.ws.controller.internal;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.ws.converter.converters.ExternalSystemConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.ExternalSystemResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Allowed for internal (other services) clients ONLY
 * Provides direct access to DAO layer
 *
 * @author Andrei Varabyeu
 */
@Controller
@RequestMapping("/api-internal")
@PreAuthorize("hasRole('COMPONENT')")
public class InternalApiController {

	private final ExternalSystemRepository externalSystemRepository;

	@Autowired
	public InternalApiController(ExternalSystemRepository externalSystemRepository) {
		this.externalSystemRepository = externalSystemRepository;
	}

	@RequestMapping(value = "/external-system/{systemId}", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public ExternalSystemResource getExternalSystem(@PathVariable String systemId) {
		ExternalSystem externalSystem = externalSystemRepository.findOne(systemId);
		BusinessRule.expect(externalSystem, Predicates.notNull()).verify(ErrorType.EXTERNAL_SYSTEM_NOT_FOUND, systemId);
		return ExternalSystemConverter.TO_RESOURCE.apply(externalSystem);
	}
}
