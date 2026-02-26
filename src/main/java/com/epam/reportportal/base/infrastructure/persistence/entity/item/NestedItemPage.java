package com.epam.reportportal.base.infrastructure.persistence.entity.item;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class NestedItemPage extends NestedItem {

  private Integer pageNumber;

  public NestedItemPage(Long id, String type, Integer logLevel, Integer pageNumber) {
    super(id, type, logLevel);
    this.pageNumber = pageNumber;
  }

  public Integer getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(Integer pageNumber) {
    this.pageNumber = pageNumber;
  }

}
