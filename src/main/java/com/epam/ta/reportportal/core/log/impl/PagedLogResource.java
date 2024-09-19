package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.model.log.LogResource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class PagedLogResource extends LogResource {

  private List<Map.Entry<Long, Integer>> pagesLocation;

  public PagedLogResource() {
    pagesLocation = new LinkedList<>();
  }

  public List<Map.Entry<Long, Integer>> getPagesLocation() {
    return pagesLocation;
  }

  public void setPagesLocation(List<Map.Entry<Long, Integer>> pagesLocation) {
    this.pagesLocation = pagesLocation;
  }
}
