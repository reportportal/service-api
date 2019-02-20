package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidgetId;
import com.epam.ta.reportportal.entity.filter.FilterSort;
import com.epam.ta.reportportal.entity.filter.ObjectType;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.ws.model.Position;
import com.epam.ta.reportportal.ws.model.Size;
import com.epam.ta.reportportal.ws.model.activity.WidgetActivityResource;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.data.domain.Sort;

import java.util.HashMap;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class WidgetConverterTest {

	@Test
	public void toWidgetResource() {
		final Widget widget = getWidget();
		final WidgetResource resource = WidgetConverter.TO_WIDGET_RESOURCE.apply(widget);

		assertEquals(resource.getName(), widget.getName());
		assertEquals(resource.getWidgetId(), widget.getId());
		assertEquals(resource.getWidgetType(), widget.getWidgetType());
		assertEquals(resource.getDescription(), widget.getDescription());
		assertEquals(resource.getOwner(), widget.getOwner());
		assertEquals(resource.getContentParameters().getItemsCount(), widget.getItemsCount());
		assertThat(resource.getContentParameters().getContentFields()).containsExactlyElementsOf(widget.getContentFields());
		assertThat(resource.getContentParameters().getWidgetOptions()).containsAllEntriesOf(widget.getWidgetOptions().getOptions());
		assertThat(resource.getAppliedFilters()).hasSize(1);
	}

	@Test
	public void toActivityResource() {
		final Widget widget = getWidget();
		final WidgetActivityResource resource = WidgetConverter.TO_ACTIVITY_RESOURCE.apply(widget);

		assertEquals(resource.getId(), widget.getId());
		assertEquals(resource.getName(), widget.getName());
		assertEquals(resource.getProjectId(), widget.getProject().getId());
		assertEquals(resource.getDescription(), widget.getDescription());
		assertEquals(resource.getItemsCount(), widget.getItemsCount());
		assertEquals(resource.getContentFields(), widget.getContentFields());
		assertThat(resource.getWidgetOptions()).containsAllEntriesOf(widget.getWidgetOptions().getOptions());
	}

	@Test
	public void toObjectModel() {
		final DashboardWidget dashboardWidget = getDashboardWIdget();
		final DashboardResource.WidgetObjectModel widgetObjectModel = WidgetConverter.TO_OBJECT_MODEL.apply(dashboardWidget);

		assertEquals(widgetObjectModel.getWidgetId(), dashboardWidget.getId().getWidgetId());
		assertEquals(widgetObjectModel.getWidgetPosition().getX(), dashboardWidget.getPositionX());
		assertEquals(widgetObjectModel.getWidgetPosition().getY(), dashboardWidget.getPositionY());
		assertEquals(widgetObjectModel.getWidgetSize().getHeight(), dashboardWidget.getHeight());
		assertEquals(widgetObjectModel.getWidgetSize().getWidth(), dashboardWidget.getWidth());
	}

	@Test
	public void toDashboardWidget() {
		final DashboardResource.WidgetObjectModel widgetObjectModel = getWidgetObjectModel();
		final Dashboard dashboard = new Dashboard();
		dashboard.setId(2L);
		dashboard.setName("name");
		final Widget widget = new Widget();
		widget.setName("name");
		widget.setShared(true);

		final DashboardWidget dashboardWidget = WidgetConverter.toDashboardWidget(widgetObjectModel, dashboard, widget);

		assertThat(dashboardWidget.getDashboard()).isEqualToComparingFieldByField(dashboard);
		assertThat(dashboardWidget.getWidget()).isEqualToComparingFieldByField(widget);
		assertEquals(dashboardWidget.getHeight(), widgetObjectModel.getWidgetSize().getHeight());
		assertEquals(dashboardWidget.getWidth(), widgetObjectModel.getWidgetSize().getWidth());
		assertEquals(dashboardWidget.getPositionX(), widgetObjectModel.getWidgetPosition().getX());
		assertEquals(dashboardWidget.getPositionY(), widgetObjectModel.getWidgetPosition().getY());
		assertEquals(dashboardWidget.getWidgetName(), widget.getName());
	}

	private Widget getWidget() {
		final Widget widget = new Widget();
		widget.setDescription("description");
		widget.setItemsCount(10);
		widget.setWidgetType("widgetType");
		widget.setName("name");
		widget.setShared(true);
		final Project project = new Project();
		project.setId(3L);
		widget.setProject(project);
		final WidgetOptions widgetOptions = new WidgetOptions();
		final HashMap<String, Object> options = new HashMap<>();
		options.put("option1", "val1");
		options.put("option2", "val2");
		widgetOptions.setOptions(options);
		widget.setWidgetOptions(widgetOptions);
		final UserFilter filter = new UserFilter();
		filter.setId(1L);
		filter.setOwner("owner");
		filter.setName("name");
		filter.setTargetClass(ObjectType.Launch);
		filter.setDescription("filter description");
		filter.setFilterCondition(Sets.newHashSet(FilterCondition.builder().eq(CRITERIA_LAUNCH_ID, "100").build()));
		widget.setFilters(Sets.newHashSet(filter));
		final FilterSort filterSort = new FilterSort();
		filterSort.setField("name");
		filterSort.setDirection(Sort.Direction.ASC);
		filterSort.setId(2L);
		filter.setFilterSorts(Sets.newHashSet(filterSort));
		return widget;
	}

	private static DashboardWidget getDashboardWIdget() {
		final DashboardWidget dashboardWidget = new DashboardWidget();
		final Widget widget = new Widget();
		widget.setId(1L);
		dashboardWidget.setWidget(widget);
		final Dashboard dashboard = new Dashboard();
		dashboard.setId(2L);
		dashboardWidget.setId(new DashboardWidgetId(dashboard.getId(), widget.getId()));
		dashboardWidget.setDashboard(dashboard);
		dashboardWidget.setPositionX(2);
		dashboardWidget.setPositionY(4);
		dashboardWidget.setHeight(5);
		dashboardWidget.setWidth(6);
		return dashboardWidget;
	}

	private static DashboardResource.WidgetObjectModel getWidgetObjectModel() {
		final DashboardResource.WidgetObjectModel widgetObjectModel = new DashboardResource.WidgetObjectModel();
		widgetObjectModel.setWidgetId(1L);
		widgetObjectModel.setWidgetPosition(new Position(3, 5));
		widgetObjectModel.setWidgetSize(new Size(4, 8));
		return widgetObjectModel;
	}

}