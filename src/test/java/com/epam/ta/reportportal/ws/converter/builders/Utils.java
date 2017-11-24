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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.database.entity.*;
import com.epam.ta.reportportal.database.entity.Dashboard.WidgetObject;
import com.epam.ta.reportportal.database.entity.filter.SelectionOptions;
import com.epam.ta.reportportal.database.entity.filter.SelectionOrder;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.filter.*;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.epam.ta.reportportal.ws.converter.builders.BuilderTestsConstants.*;

public class Utils {

	private Utils() {

	}

	private static Set<String> tags;

	static {
		tags = new HashSet<String>() {
			private static final long serialVersionUID = 1L;

			{
				add(TAG);
			}
		};
	}

	public static Launch getLaunch() {
		Launch testLaunch = new Launch();
		testLaunch.setDescription(BuilderTestsConstants.DESCRIPTION);
		testLaunch.setName(BuilderTestsConstants.DESCRIPTION);
		testLaunch.setStartTime(BuilderTestsConstants.DATE_START);
		testLaunch.setTags(tags);
		testLaunch.setEndTime(BuilderTestsConstants.DATE_END);
		testLaunch.setStatus(Status.IN_PROGRESS);
		testLaunch.setUserRef(getUser().getId());
		testLaunch.setProjectRef(getProject().getId());
		testLaunch.setMode(Mode.DEBUG);
		testLaunch.setProjectRef(getProject().getName());
		return testLaunch;
	}

	public static StartLaunchRQ getStartLaunchRQ() {
		StartLaunchRQ rq = new StartLaunchRQ();
		rq.setDescription(BuilderTestsConstants.DESCRIPTION);
		rq.setName(BuilderTestsConstants.DESCRIPTION);
		rq.setStartTime(DATE_START);
		rq.setTags(tags);
		return rq;
	}

	public static Project getProject() {
		Project project = new Project();
		project.setAddInfo(TAG);
		project.setCustomer(DESCRIPTION);
		project.setName(DESCRIPTION);
		return project;
	}

	public static User getUser() {
		User user = new User();
		user.setEmail(EMAIL);
		user.setLogin(NAME);
		user.setPassword(generateMD5(BuilderTestsConstants.PASSWORD));
		user.setRole(UserRole.USER);
		return user;
	}

	public static LaunchResource getLaunchResource() {
		LaunchResource resource = new LaunchResource();
		resource.setDescription(DESCRIPTION);
		resource.setName(DESCRIPTION);
		resource.setStartTime(DATE_START);
		resource.setEndTime(DATE_END);
		resource.setMode(Mode.DEBUG);
		resource.setStatus(Status.IN_PROGRESS.toString());
		resource.setOwner(getUser().getId());
		resource.setIsProcessing(false);
		return resource;
	}

	public static Log getTestLog() {
		Log testLog = new Log();
		testLog.setLogMsg(LOG_MESSAGE);
		testLog.setLogTime(LOG_TIME);
		testLog.setBinaryContent(BINARY_CONTENT);
		return testLog;
	}

	public static Log getRandomLog() {
		Log log = Utils.getTestLog();
		log.setId(null);
		log.setTestItemRef("");
		log.setLogMsg(log.getLogMsg() + Double.toString(Math.random()));
		return log;
	}

	public static Launch getRandomLaunch() {
		Launch launch = Utils.getLaunch();
		launch.setId(null);
		launch.setProjectRef(null);
		launch.setDescription(launch.getDescription() + Double.toString(Math.random()));
		return launch;
	}

	public static Project getRandomProject() {
		Project project = Utils.getProject();
		project.setName(null);
		return project;
	}

	public static UserFilter getUserFilter() {
		UserFilter userFilter = new UserFilter();
		SelectionOptions selectionOptions = new SelectionOptions();
		SelectionOrder selectionOrder = new SelectionOrder();
		selectionOrder.setIsAsc(true);
		selectionOrder.setSortingColumnName("name");
		selectionOptions.setOrders(Collections.singletonList(selectionOrder));
		selectionOptions.setPageNumber(43);
		userFilter.setIsLink(true);
		userFilter.setSelectionOptions(selectionOptions);
		userFilter.setName(BuilderTestsConstants.NAME);
		userFilter.setOwner("default");
		Filter filter = new Filter(Launch.class, Condition.EQUALS, false, "Demo Run from CI", BuilderTestsConstants.NAME_CRITERIA);
		userFilter.setFilter(filter);
		return userFilter;
	}

	public static CreateUserFilterRQ getUserFilterRQ() {
		CreateUserFilterRQ rq = new CreateUserFilterRQ();
		rq.setIsLink(true);
		rq.setName(BuilderTestsConstants.NAME);
		rq.setObjectType(BuilderTestsConstants.LAUNCH);
		UserFilterEntity entity = new UserFilterEntity();
		entity.setCondition(Condition.EQUALS.getMarker());
		entity.setFilteringField(BuilderTestsConstants.NAME_CRITERIA);
		entity.setValue("Demo Run from CI");
		Set<UserFilterEntity> entities = new HashSet<>();
		entities.add(entity);
		rq.setEntities(entities);
		SelectionParameters selectionParameters = new SelectionParameters();
		Order order = new Order();
		order.setIsAsc(true);
		order.setSortingColumnName("name");
		selectionParameters.setOrders(Collections.singletonList(order));
		selectionParameters.setPageNumber(43);
		rq.setSelectionParameters(selectionParameters);
		return rq;
	}

	public static WidgetRQ getWidgetRQ() {
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setContentParameters(new ContentParameters());
		widgetRQ.setName(BuilderTestsConstants.NAME);
		return widgetRQ;
	}

	public static Widget getWidget() {
		Widget widget = new Widget();
		widget.setApplyingFilterId("1234");
		widget.setContentOptions(new ContentOptions());
		widget.setName(BuilderTestsConstants.NAME);
		widget.setOwner("default");
		return widget;
	}

	public static Dashboard getDashboard() {
		Dashboard dashboard = new Dashboard();
		dashboard.setName(BuilderTestsConstants.NAME);
		List<WidgetObject> widgets = new LinkedList<>();
		List<Integer> size = new ArrayList<>();
		size.add(500);
		size.add(300);
		List<Integer> position = new ArrayList<>();
		position.add(0);
		position.add(0);
		WidgetObject widget = new WidgetObject("1234", size, position);
		widgets.add(widget);
		dashboard.setWidgets(widgets);
		dashboard.setOwner("default");
		return dashboard;
	}

	public static UserFilterResource getUserFilterResource() {
		UserFilterResource filterResource = new UserFilterResource();
		filterResource.setObjectType("launch");
		filterResource.setName(BuilderTestsConstants.NAME);
		UserFilterEntity userFilterEntity = new UserFilterEntity();
		userFilterEntity.setCondition(Condition.EQUALS.getMarker());
		userFilterEntity.setFilteringField(BuilderTestsConstants.NAME_CRITERIA);
		userFilterEntity.setValue("Demo Run from CI");
		Set<UserFilterEntity> entities = new HashSet<>();
		entities.add(userFilterEntity);
		filterResource.setEntities(entities);
		SelectionParameters selectionParameters = new SelectionParameters();
		Order order = new Order();
		order.setIsAsc(true);
		order.setSortingColumnName("name");
		selectionParameters.setOrders(Collections.singletonList(order));
		selectionParameters.setPageNumber(43);
		filterResource.setSelectionParameters(selectionParameters);
		return filterResource;
	}

	public static DashboardResource getDashboardResource() {
		DashboardResource dashboardResource = new DashboardResource();
		dashboardResource.setName(BuilderTestsConstants.NAME);
		dashboardResource.setDashboardId(BuilderTestsConstants.BINARY_DATA_ID);
		dashboardResource.setWidgets(Collections.emptyList());
		return dashboardResource;
	}

	public static TestItem getTestItem() {
		TestItem testItem = new TestItem();
		testItem.setName(NAME);
		testItem.setEndTime(DATE_END);
		testItem.setId("123");
		testItem.setIssue(new TestItemIssue());
		testItem.setItemDescription(DESCRIPTION);
		testItem.setLastModified(LOG_TIME);
		testItem.setLaunchRef("12345");
		testItem.setParent(BuilderTestsConstants.TESTSUITE_ID);
		testItem.setStartTime(DATE_START);
		testItem.setType(TestItemType.STEP);
		testItem.setStatistics(new Statistics(new ExecutionCounter(), new IssueCounter()));
		testItem.setStatus(Status.PASSED);
		return testItem;
	}

	public static TestItemResource getTestItemResource() {
		TestItemResource testItem = new TestItemResource();
		testItem.setName(NAME);
		testItem.setEndTime(DATE_END);
		testItem.setItemId("123");
		testItem.setIssue(new Issue());
		testItem.setDescription(DESCRIPTION);
		testItem.setParent(BuilderTestsConstants.TESTSUITE_ID);
		testItem.setStartTime(DATE_START);
		testItem.setType(TestItemType.STEP.name());
		testItem.setStatistics(new com.epam.ta.reportportal.ws.model.statistics.Statistics());
		testItem.setStatus(Status.PASSED.name());
		testItem.setPathNames(new HashMap<>());
		return testItem;
	}

	private static String generateMD5(String initial) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] byteInitial = initial.getBytes("UTF-8");
			byteInitial = md.digest(byteInitial);
			// convert the byte to hex format
			StringBuilder sb = new StringBuilder();
			for (byte aByteInitial : byteInitial) {
				sb.append(Integer.toString((aByteInitial & 0xff) + 0x100, 16).substring(1));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException("Unable apply MD5 algorithm for password hashing: ", ex);
		} catch (UnsupportedEncodingException unsEx) {
			throw new RuntimeException("Unable apply UTF-8 encoding for password string: ", unsEx);
		}
	}
}
