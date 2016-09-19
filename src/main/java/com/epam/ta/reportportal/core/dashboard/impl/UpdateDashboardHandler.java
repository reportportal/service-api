/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.core.dashboard.impl;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.ACCESS_DENIED;
import static com.epam.ta.reportportal.ws.model.ErrorType.DASHBOARD_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.DASHBOARD_UPDATE_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.RESOURCE_ALREADY_EXISTS;
import static com.epam.ta.reportportal.ws.model.ErrorType.WIDGET_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.WIDGET_NOT_FOUND_IN_DASHBOARD;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.epam.ta.reportportal.events.DashboardUpdatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.core.acl.AclUtils;
import com.epam.ta.reportportal.core.acl.SharingService;
import com.epam.ta.reportportal.core.dashboard.IUpdateDashboardHandler;
import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.Dashboard.WidgetObject;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import com.google.common.collect.Lists;

/**
 * Default implementation of {@link IUpdateDashboardHandler}
 * 
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateDashboardHandler implements IUpdateDashboardHandler {
	public static final int WIDGETS_LIMIT = 20;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	private SharingService sharingService;

	@Autowired
	private ApplicationEventPublisher eventPublisher;


	@Override
	public OperationCompletionRS updateDashboard(UpdateDashboardRQ rq, String dashboardId, String userName, String projectName) {

		StringBuilder additionalInfo = new StringBuilder();
		Dashboard dashboard = dashboardRepository.findOne(dashboardId);
		expect(dashboard, notNull()).verify(DASHBOARD_NOT_FOUND, dashboardId);

		AclUtils.validateOwner(dashboard.getAcl(), userName, dashboard.getName());
		expect(dashboard.getProjectName(), equalTo(projectName)).verify(ACCESS_DENIED);

		if (null != rq.getName()) {
			Dashboard isExist = dashboardRepository.findOneByUserProject(userName, projectName, rq.getName());
			if (isExist != null && !dashboardId.equalsIgnoreCase(isExist.getId()))
				fail().withError(RESOURCE_ALREADY_EXISTS, rq.getName());
			dashboard.setName(rq.getName().trim());
		}

		expect(null != rq.getAddWidget() && null != rq.getDeleteWidgetId()
				&& rq.getDeleteWidgetId().equalsIgnoreCase(rq.getAddWidget().getWidgetId()), equalTo(Boolean.FALSE))
						.verify(DASHBOARD_UPDATE_ERROR, "Unable delete and add the same widget simmulteniuosly.");

		// update widget (or list of widgets if one of them change position on
		// dashboard)
		if (null != rq.getWidgets()) {
			// dashboard.getWidgets()
			List<WidgetObject> update = new ArrayList<>();
			for (WidgetObject widget : dashboard.getWidgets()) {
				rq.getWidgets().stream().filter(updWidget -> widget.getWidgetId().equalsIgnoreCase(updWidget.getWidgetId()))
						.forEach(updWidget -> {
							if (null != updWidget.getWidgetPosition())
								widget.setWidgetPosition(updWidget.getWidgetPosition());
							if (null != updWidget.getWidgetSize())
								widget.setWidgetSize(updWidget.getWidgetSize());
						});
				update.add(widget);
			}
			dashboard.setWidgets(update);
		}

		// add widget
		if (null != rq.getAddWidget()) {
			Widget widget = widgetRepository.findOneLoadACL(rq.getAddWidget().getWidgetId());
			validateAddingWidget(dashboard.getWidgets(), widget, rq.getAddWidget().getWidgetId(), userName, projectName);

			// add widget position
			if ((null == rq.getAddWidget().getWidgetPosition()) || (rq.getAddWidget().getWidgetPosition().size() < 2)) {
				List<Integer> widgetPosition = new ArrayList<>();
				int yPosition = 0;
				int xPosition = 0;
				List<WidgetObject> widgets = dashboard.getWidgets();
				for (WidgetObject widgetObject : widgets) {
					if ((widgetObject.getWidgetPosition().size() > 1) && (widgetObject.getWidgetPosition().get(1) > yPosition)) {
						yPosition = widgetObject.getWidgetPosition().get(1);
					}
				}
				widgetPosition.add(xPosition);
				widgetPosition.add(++yPosition);
				rq.getAddWidget().setWidgetPosition(widgetPosition);
			}

			dashboard.getWidgets()
					.add(new WidgetObject(widget.getId(), rq.getAddWidget().getWidgetSize(), rq.getAddWidget().getWidgetPosition()));

			// Share all information on already shared dashboard (i.e. widget
			// and filter)
			if (!dashboard.getAcl().getEntries().isEmpty() && widget.getAcl().getEntries().isEmpty()) {
				sharingService.modifySharing(Lists.newArrayList(dashboard), userName, projectName, true);
				additionalInfo.append("Widget '");
				additionalInfo.append(rq.getAddWidget().getWidgetId());
				additionalInfo.append("' has been shared for project cause shared dashboard.");
			}
		}

		// remove widget
		if (null != rq.getDeleteWidgetId()) {
			expect(processWidgets(dashboard.getWidgets(), rq.getDeleteWidgetId(), true), equalTo(true))
					.verify(WIDGET_NOT_FOUND_IN_DASHBOARD, rq.getDeleteWidgetId(), dashboardId);
			Widget widget = widgetRepository.findOneLoadACL(rq.getDeleteWidgetId());
			if (null != widget && widget.getAcl().getOwnerUserId().equals(userName)) {
				try {
					widgetRepository.delete(rq.getDeleteWidgetId());
				} catch (Exception e) {
					throw new ReportPortalException("Error during deleting widget", e);
				}
			} else {
				Iterator<WidgetObject> iterator = dashboard.getWidgets().iterator();
				while (iterator.hasNext()) {
					if (iterator.next().getWidgetId().equals(rq.getDeleteWidgetId())) {
						iterator.remove();
						break;
					}
				}
			}
		}

		if (null != rq.getShare()) {
			sharingService.modifySharing(Lists.newArrayList(dashboard), userName, projectName, rq.getShare());
		}

		dashboardRepository.save(dashboard);

		eventPublisher.publishEvent(new DashboardUpdatedEvent(dashboard, rq, userName));
		return new OperationCompletionRS(
				"Dashboard with ID = '" + dashboard.getId() + "' successfully updated." + additionalInfo.toString());
	}

	private void validateAddingWidget(List<WidgetObject> allWidgets, Widget widgetFromDB, String widgetId, String userName,
			String projectName) {

		expect(widgetFromDB, notNull()).verify(WIDGET_NOT_FOUND, widgetId);

		expect(widgetFromDB.getProjectName(), equalTo(projectName)).verify(ErrorType.FORBIDDEN_OPERATION,
				"Impossible to add widget from another project");

		expect(allWidgets.size() == WIDGETS_LIMIT, equalTo(false)).verify(DASHBOARD_UPDATE_ERROR,
				formattedSupplier("Unable to add more than '{}' widgets to dashboard.", WIDGETS_LIMIT));

		expect(processWidgets(allWidgets, widgetId, false), equalTo(false)).verify(DASHBOARD_UPDATE_ERROR,
				formattedSupplier("Widget with ID '{}' already added to the current dashboard.", widgetId));

		AclUtils.isPossibleToRead(widgetFromDB.getAcl(), userName, projectName);
	}

	/**
	 * Iterate over Widget's Set find widget with specified id and remove it if
	 * required
	 * 
	 * @param widgets
	 * @param searchingId
	 * @param isRemove
	 * @return isFound
	 */
	private boolean processWidgets(List<WidgetObject> widgets, String searchingId, boolean isRemove) {
		Iterator<WidgetObject> iterator = widgets.iterator();
		boolean isFound = false;
		while (iterator.hasNext()) {
			WidgetObject widget = iterator.next();
			if (widget.getWidgetId().equalsIgnoreCase(searchingId)) {
				if (isRemove) {
					iterator.remove();
				}
				isFound = true;
				break;
			}
		}
		return isFound;
	}
}