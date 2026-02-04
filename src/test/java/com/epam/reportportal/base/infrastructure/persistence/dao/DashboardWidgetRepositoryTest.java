package com.epam.reportportal.base.infrastructure.persistence.dao;

import com.epam.reportportal.base.infrastructure.persistence.dao.DashboardWidgetRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Sql("/db/fill/dashboard-widget/dashboard-widget-fill.sql")
class DashboardWidgetRepositoryTest extends BaseMvcTest {

  @Autowired
  private DashboardWidgetRepository dashboardWidgetRepository;

  @Test
  void countAllByWidgetId() {
    Assertions.assertEquals(3, dashboardWidgetRepository.countAllByWidgetId(5L));
    Assertions.assertEquals(2, dashboardWidgetRepository.countAllByWidgetId(6L));
  }

}
