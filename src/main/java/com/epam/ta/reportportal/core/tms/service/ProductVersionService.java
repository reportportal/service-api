package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.dto.ProductVersionRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsProductVersionRS;

public interface ProductVersionService extends CrudService<ProductVersionRQ,
                                                           TmsProductVersionRS, Long> {
}
