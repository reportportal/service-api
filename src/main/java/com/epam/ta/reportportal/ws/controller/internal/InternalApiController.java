package com.epam.ta.reportportal.ws.controller.internal;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.ws.converter.ExternalSystemResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.ExternalSystemResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Provider;

/**
 * Allowed for internal (other services) clients ONLY
 * Provides direct access to DAO layer
 *
 * @author Andrei Varabyeu
 */
@Controller
@RequestMapping("/api-internal")
public class InternalApiController {

	@Autowired
	private ExternalSystemRepository externalSystemRepository;

	@Autowired
	private Provider<ExternalSystemResourceAssembler> externalSystemResourceAssembler;

	@RequestMapping(value = "/external-system/{systemId}", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public ExternalSystemResource getExternalSystem(@PathVariable String systemId) {
		ExternalSystem externalSystem = externalSystemRepository.findOne(systemId);
		BusinessRule.expect(externalSystem, Predicates.notNull()).verify(ErrorType.EXTERNAL_SYSTEM_NOT_FOUND, systemId);
		return externalSystemResourceAssembler.get().toResource(externalSystem);
	}
}
