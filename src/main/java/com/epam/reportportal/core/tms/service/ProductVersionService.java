package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.ProductVersionRQ;
import com.epam.reportportal.core.tms.dto.TmsProductVersionRS;

public interface ProductVersionService extends CrudService<ProductVersionRQ,
    TmsProductVersionRS, Long> {

}
