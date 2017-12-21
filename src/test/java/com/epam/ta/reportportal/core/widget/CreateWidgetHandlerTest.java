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

package com.epam.ta.reportportal.core.widget;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.widget.content.GadgetTypes;
import com.epam.ta.reportportal.core.widget.content.WidgetDataTypes;
import com.epam.ta.reportportal.core.widget.impl.CreateWidgetHandler;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.CriteriaMap;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationEventPublisher;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

/**
 * @author Dzmitry_Kavalets
 */
public class CreateWidgetHandlerTest {

	private static final String DEFAULT_USER = "user2";
	private static final String DEFAULT_PROJECT = "default_project";
	private static final String FILTER_ID = "filterId";
	private static final List<String> ACTIVITY_FIELDS = Arrays.asList("userRef", "loggedObjectRef", "last_modified", "objectType",
			"actionType", "projectRef", "history"
	);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static Provider<WidgetBuilder> lazyReference;
	private static WidgetRepository widgetRepository;
	private static UserFilterRepository userFilterRepository;
	private static ApplicationEventPublisher eventPublisher;
	private static CreateWidgetHandler createWidgetHandler;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void beforeClass() {
		lazyReference = mock(Provider.class);
		when(lazyReference.get()).thenReturn(new WidgetBuilder());
		widgetRepository = mock(WidgetRepository.class);
		when(widgetRepository.findByProjectAndUser(DEFAULT_PROJECT, DEFAULT_USER)).thenReturn(new ArrayList<>());
		userFilterRepository = mock(UserFilterRepository.class);
		when(userFilterRepository.findOneLoadACL(DEFAULT_USER, FILTER_ID, DEFAULT_PROJECT)).thenReturn(new UserFilter());
		eventPublisher = mock(ApplicationEventPublisher.class);
		doNothing().when(eventPublisher).publishEvent(anyObject());
	}

	@Test
	public void createActivityWidgetPositive() {
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setGadget(GadgetTypes.ACTIVITY.getType());
		contentParameters.setType(WidgetDataTypes.ACTIVITY.getType());
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setName("activityWidget");
		widgetRQ.setContentParameters(contentParameters);
		widgetRQ.setFilterId(FILTER_ID);

		final CreateWidgetHandler createWidgetHandler = new CreateWidgetHandler();
		createWidgetHandler.setWidgetRepository(widgetRepository);
		createWidgetHandler.setUserFilterRepository(userFilterRepository);
		createWidgetHandler.setCriteriaMapFactory(mock(CriteriaMapFactory.class));
		createWidgetHandler.setWidgetBuilder(lazyReference);
		createWidgetHandler.setEventPublisher(eventPublisher);

		final EntryCreatedRS widget = createWidgetHandler.createWidget(widgetRQ, DEFAULT_PROJECT, DEFAULT_USER);
		Assert.assertNotNull(widget);
	}

	@Test
	public void incorrectWidgetType() {
		ContentParameters contentParameters = new ContentParameters();
		final String type = "randomWidgetType";
		contentParameters.setType(type);
		contentParameters.setGadget(WidgetDataTypes.ACTIVITY.getType());
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setName("activityWidget");
		widgetRQ.setContentParameters(contentParameters);
		widgetRQ.setFilterId(FILTER_ID);

		final CreateWidgetHandler createWidgetHandler = new CreateWidgetHandler();
		createWidgetHandler.setWidgetRepository(widgetRepository);
		createWidgetHandler.setUserFilterRepository(userFilterRepository);
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(Suppliers.formattedSupplier(
				"Unknown widget data type: '{}'. " + "Possible data types: line_chart, bar_chart, column_chart, combine_pie_chart, table",
				type
		).get());
		createWidgetHandler.createWidget(widgetRQ, DEFAULT_PROJECT, DEFAULT_USER);
	}

	@Test
	public void incorrectGadgetType() {
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setType(WidgetDataTypes.ACTIVITY.getType());
		final String gadget = "randomGadgetType";
		contentParameters.setGadget(gadget);
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setName("activityWidget");
		widgetRQ.setContentParameters(contentParameters);
		widgetRQ.setFilterId(FILTER_ID);

		final CreateWidgetHandler createWidgetHandler = new CreateWidgetHandler();
		createWidgetHandler.setWidgetRepository(widgetRepository);
		createWidgetHandler.setUserFilterRepository(userFilterRepository);
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(Suppliers.formattedSupplier("Unknown gadget type: '{}'.", gadget).get());
		createWidgetHandler.createWidget(widgetRQ, DEFAULT_PROJECT, DEFAULT_USER);
	}

	@Test
	public void emptyFilter() {
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setGadget(GadgetTypes.LAUNCHES_TABLE.getType());
		contentParameters.setType(WidgetDataTypes.LAUNCHES_TABLE.getType());
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setContentParameters(contentParameters);
		widgetRQ.setName("withoutFilter");

		final CreateWidgetHandler createWidgetHandler = new CreateWidgetHandler();
		createWidgetHandler.setWidgetRepository(widgetRepository);
		createWidgetHandler.setUserFilterRepository(mock(UserFilterRepository.class));

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(
				Suppliers.formattedSupplier("User filter '{}' not found for user '{}'. Did you use correct User Filter ID?", null,
						DEFAULT_USER
				).get());
		createWidgetHandler.createWidget(widgetRQ, DEFAULT_PROJECT, DEFAULT_USER);
	}

	@Test
	public void contentFieldsActivityNegative() {
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setGadget(GadgetTypes.ACTIVITY.getType());
		contentParameters.setType(WidgetDataTypes.ACTIVITY.getType());
		contentParameters.setContentFields(Arrays.asList("negativeFirst", "negativeSecond"));
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setContentParameters(contentParameters);
		widgetRQ.setName("contentFieldsNegative");

		final UserFilterRepository userFilterRepository = mock(UserFilterRepository.class);
		final UserFilter userFilter = new UserFilter();
		userFilter.setFilter(new Filter(Launch.class, Condition.EQUALS, false, "name", Launch.NAME));
		when(userFilterRepository.findOneLoadACL(DEFAULT_USER, widgetRQ.getFilterId(), DEFAULT_PROJECT)).thenReturn(userFilter);

		final CriteriaMapFactory criteriaMapFactory = mock(CriteriaMapFactory.class);
		when(criteriaMapFactory.getCriteriaMap(Activity.class)).thenReturn(new CriteriaMap<>(Activity.class));

		final CreateWidgetHandler createWidgetHandler = new CreateWidgetHandler();
		createWidgetHandler.setWidgetRepository(widgetRepository);
		createWidgetHandler.setUserFilterRepository(userFilterRepository);
		createWidgetHandler.setCriteriaMapFactory(criteriaMapFactory);
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(
				Suppliers.formattedSupplier("Field '{}' cannot be used for calculating data for widget.", "negativeFirst").get());
		createWidgetHandler.createWidget(widgetRQ, DEFAULT_PROJECT, DEFAULT_USER);
	}

	@Test
	public void contentFieldsActivityPositive() {
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setGadget(GadgetTypes.ACTIVITY.getType());
		contentParameters.setType(WidgetDataTypes.ACTIVITY.getType());
		contentParameters.setContentFields(ACTIVITY_FIELDS);
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setContentParameters(contentParameters);
		widgetRQ.setName("contentFieldsPositive");

		final UserFilterRepository userFilterRepository = mock(UserFilterRepository.class);
		final UserFilter userFilter = new UserFilter();
		userFilter.setFilter(new Filter(Launch.class, Condition.EQUALS, false, "name", Launch.NAME));
		when(userFilterRepository.findOneLoadACL(DEFAULT_USER, widgetRQ.getFilterId(), DEFAULT_PROJECT)).thenReturn(userFilter);

		final CriteriaMapFactory criteriaMapFactory = mock(CriteriaMapFactory.class);
		when(criteriaMapFactory.getCriteriaMap(Activity.class)).thenReturn(new CriteriaMap<>(Activity.class));

		final CreateWidgetHandler createWidgetHandler = new CreateWidgetHandler();
		createWidgetHandler.setWidgetRepository(widgetRepository);
		createWidgetHandler.setUserFilterRepository(userFilterRepository);
		createWidgetHandler.setCriteriaMapFactory(criteriaMapFactory);
		createWidgetHandler.setWidgetBuilder(lazyReference);
		createWidgetHandler.setEventPublisher(eventPublisher);

		final EntryCreatedRS widget = createWidgetHandler.createWidget(widgetRQ, DEFAULT_PROJECT, DEFAULT_USER);
		Assert.assertNotNull(widget);
	}

	@Test
	public void metadataFieldsActivityNegative() {
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setGadget(GadgetTypes.ACTIVITY.getType());
		contentParameters.setType(WidgetDataTypes.ACTIVITY.getType());
		contentParameters.setMetadataFields(Arrays.asList("negativeFirst", "negativeSecond"));
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setContentParameters(contentParameters);
		widgetRQ.setName("metadataFieldsNegative");

		final UserFilterRepository userFilterRepository = mock(UserFilterRepository.class);
		final UserFilter userFilter = new UserFilter();
		userFilter.setFilter(new Filter(Launch.class, Condition.EQUALS, false, "name", Launch.NAME));
		when(userFilterRepository.findOneLoadACL(DEFAULT_USER, widgetRQ.getFilterId(), DEFAULT_PROJECT)).thenReturn(userFilter);

		final CriteriaMapFactory criteriaMapFactory = mock(CriteriaMapFactory.class);
		when(criteriaMapFactory.getCriteriaMap(Activity.class)).thenReturn(new CriteriaMap<>(Activity.class));

		final CreateWidgetHandler createWidgetHandler = new CreateWidgetHandler();
		createWidgetHandler.setWidgetRepository(widgetRepository);
		createWidgetHandler.setUserFilterRepository(userFilterRepository);
		createWidgetHandler.setCriteriaMapFactory(criteriaMapFactory);
		createWidgetHandler.setEventPublisher(eventPublisher);
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(
				Suppliers.formattedSupplier("Field '{}' cannot be used for calculating data for widget.", "negativeFirst").get());
		createWidgetHandler.createWidget(widgetRQ, DEFAULT_PROJECT, DEFAULT_USER);
	}

	@Test
	public void metadataFieldsActivityPositive() {
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setGadget(GadgetTypes.ACTIVITY.getType());
		contentParameters.setType(WidgetDataTypes.ACTIVITY.getType());
		contentParameters.setMetadataFields(ACTIVITY_FIELDS);
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setContentParameters(contentParameters);
		widgetRQ.setName("metadataFieldsPositive");

		final UserFilterRepository userFilterRepository = mock(UserFilterRepository.class);
		final UserFilter userFilter = new UserFilter();
		userFilter.setFilter(new Filter(Launch.class, Condition.EQUALS, false, "name", Launch.NAME));
		when(userFilterRepository.findOneLoadACL(DEFAULT_USER, widgetRQ.getFilterId(), DEFAULT_PROJECT)).thenReturn(userFilter);

		final CriteriaMapFactory criteriaMapFactory = mock(CriteriaMapFactory.class);
		when(criteriaMapFactory.getCriteriaMap(Activity.class)).thenReturn(new CriteriaMap<>(Activity.class));

		final CreateWidgetHandler createWidgetHandler = new CreateWidgetHandler();

		createWidgetHandler.setWidgetRepository(widgetRepository);
		createWidgetHandler.setUserFilterRepository(userFilterRepository);
		createWidgetHandler.setCriteriaMapFactory(criteriaMapFactory);
		createWidgetHandler.setWidgetBuilder(lazyReference);
		createWidgetHandler.setEventPublisher(eventPublisher);

		final EntryCreatedRS widget = createWidgetHandler.createWidget(widgetRQ, DEFAULT_PROJECT, DEFAULT_USER);
		Assert.assertNotNull(widget);
	}

	@Test
	public void alreadyExistingWidgetName() {
		final String widget1 = "widget1";
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setGadget(GadgetTypes.ACTIVITY.getType());
		contentParameters.setType(WidgetDataTypes.ACTIVITY.getType());
		contentParameters.setContentFields(ACTIVITY_FIELDS);
		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setContentParameters(contentParameters);
		widgetRQ.setName(widget1);

		final WidgetRepository widgetRepository = mock(WidgetRepository.class);
		final Widget widget = new Widget();
		widget.setName(widget1);
		when(widgetRepository.findByProjectAndUser(DEFAULT_PROJECT, DEFAULT_USER)).thenReturn(Collections.singletonList(widget));
		final CreateWidgetHandler createWidgetHandler = new CreateWidgetHandler();
		createWidgetHandler.setWidgetRepository(widgetRepository);

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(Suppliers.formattedSupplier(ErrorType.RESOURCE_ALREADY_EXISTS.getDescription(), widget1).get());
		createWidgetHandler.createWidget(widgetRQ, DEFAULT_PROJECT, DEFAULT_USER);
	}
}