package com.epam.ta.reportportal.core.events.activity.item;

import com.epam.ta.reportportal.core.events.ProjectIdAwareEvent;
import com.epam.ta.reportportal.entity.item.TestItem;

public class TestItemFinishedEvent implements ProjectIdAwareEvent {

  private final TestItem testItem;

  private final Long projectId;

  public TestItemFinishedEvent(TestItem testItem, Long projectId) {
    this.testItem = testItem;
    this.projectId = projectId;
  }

  public TestItem getTestItem() {
    return testItem;
  }

  @Override
  public Long getProjectId() {
    return projectId;
  }
}
