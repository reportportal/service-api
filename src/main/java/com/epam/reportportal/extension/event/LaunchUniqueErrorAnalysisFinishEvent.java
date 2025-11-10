package com.epam.reportportal.extension.event;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class LaunchUniqueErrorAnalysisFinishEvent extends LaunchEvent<Long> {

  public LaunchUniqueErrorAnalysisFinishEvent(Long id) {
    super(id);
  }

}
