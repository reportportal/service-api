package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.UserConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Transactional(readOnly = true)
	@GetMapping(value = { "", "/" })
	@ApiOperation("Return information about current logged-in user")
	public UserResource getMyself(@AuthenticationPrincipal ReportPortalUser user) {
		return UserConverter.TO_RESOURCE.apply(userRepository.findByLogin(EntityUtils.normalizeId(user.getUsername()))
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, user.getUsername())));
	}

}
