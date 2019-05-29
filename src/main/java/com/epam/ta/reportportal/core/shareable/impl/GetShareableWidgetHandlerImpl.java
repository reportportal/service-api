package com.epam.ta.reportportal.core.shareable.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.auth.permissions.Permissions.CAN_ADMINISTRATE_OBJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.CAN_READ_OBJECT;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class GetShareableWidgetHandlerImpl implements GetShareableEntityHandler<Widget> {

	private final WidgetRepository widgetRepository;

	@Autowired
	public GetShareableWidgetHandlerImpl(WidgetRepository widgetRepository) {
		this.widgetRepository = widgetRepository;
	}

	@Override
	@PostAuthorize(CAN_READ_OBJECT)
	public Widget getPermitted(Long id, ReportPortalUser.ProjectDetails projectDetails) {
		return widgetRepository.findByIdAndProjectId(id, projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND_IN_PROJECT, id, projectDetails.getProjectName()));
	}

	@Override
	@PostAuthorize(CAN_ADMINISTRATE_OBJECT)
	public Widget getAdministrated(Long id, ReportPortalUser.ProjectDetails projectDetails) {
		return widgetRepository.findByIdAndProjectId(id, projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND_IN_PROJECT, id, projectDetails.getProjectName()));
	}
}
