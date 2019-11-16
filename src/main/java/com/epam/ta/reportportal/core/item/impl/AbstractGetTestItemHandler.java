package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;

import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.project.ProjectRole.OPERATOR;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractGetTestItemHandler {

	private final LaunchRepository launchRepository;

	protected AbstractGetTestItemHandler(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	protected void validate(Long launchId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId).orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
					formattedSupplier("Specified launch with id '{}' not referenced to specified project with id '{}'",
							launch.getId(),
							projectDetails.getProjectId()
					)
			);
			expect(projectDetails.getProjectRole() == OPERATOR && launch.getMode() == LaunchModeEnum.DEBUG,
					Predicate.isEqual(false)
			).verify(ACCESS_DENIED);
		}
	}

	protected void validateProjectRole(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(projectDetails.getProjectRole() == OPERATOR, Predicate.isEqual(false)).verify(ACCESS_DENIED);
		}
	}

}
